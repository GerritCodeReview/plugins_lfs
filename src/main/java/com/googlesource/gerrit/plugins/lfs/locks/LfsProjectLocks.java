// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.lfs.locks;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.lfs.locks.LfsLocksHandler.LfsLockExistsException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lfs.errors.LfsException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LfsProjectLocks {
  interface Factory {
    LfsProjectLocks create(Project.NameKey project);
  }

  private static final Logger log = LoggerFactory.getLogger(LfsProjectLocks.class);
  private static final DateTimeFormatter ISO = ISODateTimeFormat.dateTime();
  private static final Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .disableHtmlEscaping()
          .create();

  private final PathToLockId toLockId;
  private final String project;
  private final Path locksPath;
  private final Cache<String, LfsLock> locks;

  @Inject
  LfsProjectLocks(
      PathToLockId toLockId, LfsLocksPathProvider locksPath, @Assisted Project.NameKey project) {
    this.toLockId = toLockId;
    this.project = project.get();
    this.locksPath = Paths.get(locksPath.get(), this.project);
    this.locks = CacheBuilder.newBuilder().build();
  }

  void load() {
    if (!Files.exists(locksPath)) {
      return;
    }
    try {
      Files.list(locksPath)
          .filter(Files::isRegularFile)
          .forEach(
              path -> {
                if (!Files.isReadable(path)) {
                  log.warn("Lock file [{}] in project {} is not readable", path, project);
                  return;
                }

                try (Reader in = Files.newBufferedReader(path)) {
                  LfsLock lock = gson.fromJson(in, LfsLock.class);
                  locks.put(lock.id, lock);
                } catch (IOException e) {
                  log.warn("Reading lock [{}] failed", path, e);
                }
              });
    } catch (IOException e) {
      log.warn("Reading locks in project {} failed", project, e);
    }
  }

  Optional<LfsLock> getLock(String lockId) {
    return Optional.ofNullable(locks.getIfPresent(lockId));
  }

  LfsLock createLock(CurrentUser user, LfsCreateLockInput input) throws LfsException {
    log.debug("Create lock for {} in project {}", input.path, project);
    String lockId = toLockId.apply(input.path);
    LfsLock lock = locks.getIfPresent(lockId);
    if (lock != null) {
      throw new LfsLockExistsException(lock);
    }

    lock = new LfsLock(lockId, input.path, now(), new LfsLockOwner(user.getUserName()));
    LockFile fileLock = new LockFile(locksPath.resolve(lockId).toFile());
    try {
      if (!fileLock.lock()) {
        log.warn("Cannot lock path [{}] in project {}", input.path, project);
        throw new LfsLockExistsException(lock);
      }
    } catch (IOException e) {
      String error =
          String.format(
              "Locking path [%s] in project %s failed with error %s",
              input.path, project, e.getMessage());
      log.warn(error);
      throw new LfsException(error);
    }

    try {
      try (OutputStreamWriter out = new OutputStreamWriter(fileLock.getOutputStream())) {
        gson.toJson(lock, out);
      } catch (IOException e) {
        String error =
            String.format(
                "Locking path [%s] in project %s failed during write with error %s",
                input.path, project, e.getMessage());
        log.warn(error);
        throw new LfsException(error);
      }
      if (!fileLock.commit()) {
        String error =
            String.format("Committing lock to path [%s] in project %s failed", input.path, project);
        log.warn(error);
        throw new LfsException(error);
      }
      // put lock object to cache while file lock is being hold so that
      // there is no chance that other process performs lock operation
      // in the meantime (either cache returns with existing object or
      // LockFile.lock fails on locking attempt)
      locks.put(lockId, lock);
    } finally {
      fileLock.unlock();
    }

    return lock;
  }

  void deleteLock(LfsLock lock) throws LfsException {
    LockFile fileLock = new LockFile(locksPath.resolve(lock.id).toFile());
    try {
      if (!fileLock.lock()) {
        String error =
            String.format(
                "Deleting lock on path [%s] in project %s is not possible", lock.path, project);
        log.warn(error);
        throw new LfsException(error);
      }
    } catch (IOException e) {
      String error =
          String.format(
              "Getting lock on path [%s] in project %s failed with error %s",
              lock.path, project, e.getMessage());
      log.warn(error);
      throw new LfsException(error);
    }

    try {
      Files.deleteIfExists(locksPath.resolve(lock.id));
      locks.invalidate(lock.id);
    } catch (IOException e) {
      String error =
          String.format(
              "Deleting lock on path [%s] in project %s failed with error %s",
              lock.path, project, e.getMessage());
      log.warn(error);
      throw new LfsException(error);
    } finally {
      fileLock.unlock();
    }
  }

  Collection<LfsLock> getLocks() {
    return locks.asMap().values();
  }

  private String now() {
    return ISO.print(DateTime.now().toDateTime(DateTimeZone.UTC));
  }
}
