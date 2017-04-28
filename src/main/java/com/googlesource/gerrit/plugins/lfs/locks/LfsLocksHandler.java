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
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gerrit.server.CurrentUser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.jgit.lfs.errors.LfsException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class LfsLocksHandler {
  static class LfsLockExistsException extends LfsException {
    private static final long serialVersionUID = 1L;

    public final LfsLocksContext.Error error;

    public LfsLockExistsException(LfsLock lock) {
      super("Lock is already created");
      this.error = new LockError(getMessage(), lock);
    }
  }

  static class LockError extends LfsLocksContext.Error {
    public final LfsLock lock;

    LockError(String m, LfsLock lock) {
      super(m);
      this.lock = lock;
    }
  }

  private static final Logger log = LoggerFactory.getLogger(LfsLockExistsException.class);
  private static final DateTimeFormatter ISO = ISODateTimeFormat.dateTime();

  private final PathToLockId toLockId;
  private final LoadingCache<String, Cache<String, LfsLock>> projects;

  @Inject
  LfsLocksHandler(PathToLockId toLockId) {
    this.toLockId = toLockId;
    this.projects =
        CacheBuilder.newBuilder()
            .build(
                new CacheLoader<String, Cache<String, LfsLock>>() {
                  @Override
                  public Cache<String, LfsLock> load(String key) throws Exception {
                    return CacheBuilder.newBuilder().build();
                  }
                });
  }

  LfsLockResponse createLock(String project, CurrentUser user, LfsCreateLockInput input)
      throws LfsLockExistsException {
    log.debug("Create lock for {} in project {}", input.path, project);
    String lockId = toLockId.apply(input.path);
    Cache<String, LfsLock> locks = projects.getUnchecked(project);
    LfsLock lock = locks.getIfPresent(lockId);
    if (lock != null) {
      throw new LfsLockExistsException(lock);
    }

    lock = new LfsLock(lockId, input.path, now(), new LfsLockOwner(user.getUserName()));
    locks.put(lockId, lock);
    return new LfsLockResponse(lock);
  }

  LfsLockResponse deleteLock(
      String project, CurrentUser user, String lockId, LfsDeleteLockInput input)
      throws LfsException {
    log.debug(
        "Delete (-f {}) lock for {} in project {}",
        Boolean.TRUE.equals(input.force),
        lockId,
        project);
    Cache<String, LfsLock> locks = projects.getUnchecked(project);
    LfsLock lock = locks.getIfPresent(lockId);
    if (lock == null) {
      throw new LfsException(
          String.format("there is no lock id %s in project %s", lockId, project));
    }

    if (lock.owner.name.equals(user.getUserName())) {
      locks.invalidate(lockId);
      return new LfsLockResponse(lock);
    } else if (input.force) {
      locks.invalidate(lockId);
      return new LfsLockResponse(lock);
    }

    throw new LfsException(
        String.format("Lock %s is owned by different user %s", lockId, lock.owner.name));
  }

  LfsVerifyLocksResponse verifyLocks(String project, final CurrentUser user) {
    log.debug("Verify list of locks for {} project and user {}", project, user);
    Cache<String, LfsLock> locks = projects.getUnchecked(project);
    Function<LfsLock, Boolean> isOurs =
        new Function<LfsLock, Boolean>() {
          @Override
          public Boolean apply(LfsLock input) {
            return input.owner.name.equals(user.getUserName());
          }
        };
    Map<Boolean, List<LfsLock>> groupByOurs =
        locks.asMap().values().stream().collect(Collectors.groupingBy(isOurs));
    return new LfsVerifyLocksResponse(groupByOurs.get(true), groupByOurs.get(false), null);
  }

  LfsGetLocksResponse listLocksByPath(String project, String path) {
    log.debug("Get lock for {} path in {} project", path, project);
    String lockId = toLockId.apply(path);
    return listLocksById(project, lockId);
  }

  LfsGetLocksResponse listLocksById(String project, String id) {
    log.debug("Get lock for {} id in {} project", id, project);
    Cache<String, LfsLock> locks = projects.getUnchecked(project);
    LfsLock lock = locks.getIfPresent(id);
    List<LfsLock> locksById = (lock == null ? Collections.emptyList() : Lists.newArrayList(lock));
    return new LfsGetLocksResponse(locksById, null);
  }

  LfsGetLocksResponse listLocks(String project) {
    log.debug("Get locks for {} project", project);
    return new LfsGetLocksResponse(projects.getUnchecked(project).asMap().values(), null);
  }

  protected String now() {
    return ISO.print(DateTime.now().toDateTime(DateTimeZone.UTC));
  }
}
