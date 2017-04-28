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

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.cache.CacheModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.jgit.lfs.errors.LfsException;
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

  private static final Logger log = LoggerFactory.getLogger(LfsLocksHandler.class);
  private static final String CACHE_NAME = "lfs_project_locks";

  static Module module() {
    return new CacheModule(){
      @Override
      protected void configure() {
        cache(CACHE_NAME, Project.NameKey.class, LfsProjectLocks.class)
          .loader(Loader.class);
      }
    };
  }

  private final PathToLockId toLockId;
  private final LoadingCache<Project.NameKey, LfsProjectLocks> projectLocks;

  @Inject
  LfsLocksHandler(PathToLockId toLockId,
      @Named(CACHE_NAME) LoadingCache<Project.NameKey, LfsProjectLocks> projectLocks) {
    this.toLockId = toLockId;
    this.projectLocks = projectLocks;
  }

  LfsLockResponse createLock(Project.NameKey project, CurrentUser user, LfsCreateLockInput input)
      throws LfsException {
    log.debug("Create lock for {} in project {}", input.path, project);
    LfsProjectLocks locks = projectLocks.getUnchecked(project);
    LfsLock lock = locks.createLock(user, input);
    return new LfsLockResponse(lock);
  }

  LfsLockResponse deleteLock(
      Project.NameKey project, CurrentUser user, String lockId, LfsDeleteLockInput input)
      throws LfsException {
    log.debug(
        "Delete (-f {}) lock for {} in project {}",
        Boolean.TRUE.equals(input.force),
        lockId,
        project);
    LfsProjectLocks locks = projectLocks.getUnchecked(project);
    Optional<LfsLock> hasLock = locks.getLock(lockId);
    if (!hasLock.isPresent()) {
      throw new LfsException(
          String.format("there is no lock id %s in project %s", lockId, project));
    }

    LfsLock lock = hasLock.get();
    if (lock.owner.name.equals(user.getUserName())) {
      locks.deleteLock(lock);
      return new LfsLockResponse(lock);
    } else if (input.force) {
      locks.deleteLock(lock);
      return new LfsLockResponse(lock);
    }

    throw new LfsException(
        String.format("Lock %s is owned by different user %s", lockId, lock.owner.name));
  }

  LfsVerifyLocksResponse verifyLocks(Project.NameKey project, final CurrentUser user) {
    log.debug("Verify list of locks for {} project and user {}", project, user);
    LfsProjectLocks locks = projectLocks.getUnchecked(project);
    Function<LfsLock, Boolean> isOurs =
        new Function<LfsLock, Boolean>() {
          @Override
          public Boolean apply(LfsLock input) {
            return input.owner.name.equals(user.getUserName());
          }
        };
    Map<Boolean, List<LfsLock>> groupByOurs =
        locks.getLocks().stream().collect(Collectors.groupingBy(isOurs));
    return new LfsVerifyLocksResponse(groupByOurs.get(true), groupByOurs.get(false), null);
  }

  LfsGetLocksResponse listLocksByPath(Project.NameKey project, String path) {
    log.debug("Get lock for {} path in {} project", path, project);
    String lockId = toLockId.apply(path);
    return listLocksById(project, lockId);
  }

  LfsGetLocksResponse listLocksById(Project.NameKey project, String id) {
    log.debug("Get lock for {} id in {} project", id, project);
    LfsProjectLocks locks = projectLocks.getUnchecked(project);
    Optional<LfsLock> lock = locks.getLock(id);
    List<LfsLock> locksById =
        (lock.isPresent() ? ImmutableList.of(lock.get()) : Collections.emptyList());
    return new LfsGetLocksResponse(locksById, null);
  }

  LfsGetLocksResponse listLocks(Project.NameKey project) {
    log.debug("Get locks for {} project", project);
    return new LfsGetLocksResponse(projectLocks.getUnchecked(project).getLocks(), null);
  }

  static class Loader extends CacheLoader<Project.NameKey, LfsProjectLocks> {
    private final LfsProjectLocks.Factory factory;

    @Inject
    Loader(LfsProjectLocks.Factory factory) {
      this.factory = factory;
    }

    @Override
    public LfsProjectLocks load(Project.NameKey project) throws Exception {
      LfsProjectLocks locks = factory.create(project);
      locks.load();
      return locks;
    }
  }
}
