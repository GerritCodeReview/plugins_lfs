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
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.lfs.LfsDateTime;
import com.googlesource.gerrit.plugins.lfs.LfsGson;
import com.googlesource.gerrit.plugins.lfs.locks.LfsLocksHandler.LfsLockExistsException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lfs.errors.LfsException;

class LfsProjectLocks {
  interface Factory {
    LfsProjectLocks create(Project.NameKey project);
  }

  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private final LfsGson gson;
  private final String project;
  private final Path locksPath;
  private final Cache<String, LfsLock> locks;

  @Inject
  LfsProjectLocks(LfsGson gson, LfsLocksPathProvider locksPath, @Assisted Project.NameKey project) {
    this.gson = gson;
    this.project = project.get();
    this.locksPath = Paths.get(locksPath.get(), this.project);
    this.locks = CacheBuilder.newBuilder().build();
  }

  void load() {
    if (!Files.exists(locksPath)) {
      return;
    }
    try (Stream<Path> stream = Files.list(locksPath)) {
      stream
          .filter(Files::isRegularFile)
          .forEach(
              path -> {
                if (!Files.isReadable(path)) {
                  log.atWarning().log(
                      "Lock file [%s] in project %s is not readable", path, project);
                  return;
                }

                try (Reader in = Files.newBufferedReader(path)) {
                  LfsLock lock = gson.fromJson(in, LfsLock.class);
                  locks.put(lock.id, lock);
                } catch (IOException e) {
                  log.atWarning().withCause(e).log("Reading lock [%s] failed", path);
                }
              });
    } catch (IOException e) {
      log.atWarning().withCause(e).log("Reading locks in project %s failed", project);
    }
  }

  Optional<LfsLock> getLock(String lockId) {
    return Optional.ofNullable(locks.getIfPresent(lockId));
  }

  LfsLock createLock(CurrentUser user, LfsCreateLockInput input) throws LfsException {
    log.atFine().log("Create lock for %s in project %s", input.path, project);
    String lockId = PathToLockId.CONVERTER.convert(input.path);
    LfsLock lock = locks.getIfPresent(lockId);
    if (lock != null) {
      throw new LfsLockExistsException(lock);
    }

    lock =
        new LfsLock(
            lockId, input.path, LfsDateTime.now(), new LfsLockOwner(user.getUserName().get()));
    LockFile fileLock = new LockFile(locksPath.resolve(lockId).toFile());
    try {
      if (!fileLock.lock()) {
        log.atWarning().log("Cannot lock path [%s] in project %s", input.path, project);
        throw new LfsLockExistsException(lock);
      }
    } catch (IOException e) {
      String error =
          String.format(
              "Locking path [%s] in project %s failed with error %s",
              input.path, project, e.getMessage());
      log.atWarning().log(error);
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
        log.atWarning().log(error);
        throw new LfsException(error);
      }
      if (!fileLock.commit()) {
        String error =
            String.format("Committing lock to path [%s] in project %s failed", input.path, project);
        log.atWarning().log(error);
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
        log.atWarning().log(error);
        throw new LfsException(error);
      }
    } catch (IOException e) {
      String error =
          String.format(
              "Getting lock on path [%s] in project %s failed with error %s",
              lock.path, project, e.getMessage());
      log.atWarning().log(error);
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
      log.atWarning().log(error);
      throw new LfsException(error);
    } finally {
      fileLock.unlock();
    }
  }

  Collection<LfsLock> getLocks() {
    return locks.asMap().values();
  }
}
