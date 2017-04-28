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

import com.google.common.collect.ImmutableList;
import com.google.gerrit.server.CurrentUser;
import com.google.inject.Singleton;
import java.util.Collections;
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

  LfsLock createLock(String project, LfsCreateLockInput input) throws LfsLockExistsException {
    log.debug("Create lock for {} in project {}", input.path, project);
    //TODO: this is just the method stub lock creation
    return new LfsLock(
        "random_id", input.path, now(), new LfsLockOwner("Lock Owner <lock_owner@example.com>"));
  }

  LfsLock deleteLock(String project, String lockId, LfsDeleteLockInput input) throws LfsException {
    log.debug(
        "Delete (-f {}) lock for {} in project {}",
        Boolean.TRUE.equals(input.force),
        lockId,
        project);
    //TODO: this is just the method stub for lock deletion
    return new LfsLock(
        lockId,
        "some/path/to/file",
        now(),
        new LfsLockOwner("Lock Owner <lock_owner@example.com>"));
  }

  LfsVerifyLocksResponse verifyLocks(String project, CurrentUser user) {
    log.debug("Verify list of locks for {} project and user {}", project, user);
    //TODO method stub for verifying locks
    return new LfsVerifyLocksResponse(Collections.emptyList(), Collections.emptyList(), null);
  }

  LfsGetLocksResponse listLocksByPath(String project, String path) {
    log.debug("Get lock for {} path in {} project", path, project);
    // stub for searching lock by path
    return new LfsGetLocksResponse(
        ImmutableList.<LfsLock>builder()
            .add(
                new LfsLock(
                    "random_id",
                    path,
                    now(),
                    new LfsLockOwner("Lock Owner <lock_owner@example.com>")))
            .build(),
        null);
  }

  LfsGetLocksResponse listLocksById(String project, String id) {
    log.debug("Get lock for {} id in {} project", id, project);
    // stub for searching lock by id
    return new LfsGetLocksResponse(
        ImmutableList.<LfsLock>builder()
            .add(
                new LfsLock(
                    id,
                    "path/to/file",
                    now(),
                    new LfsLockOwner("Lock Owner <lock_owner@example.com>")))
            .build(),
        null);
  }

  LfsGetLocksResponse listLocks(String project) {
    log.debug("Get locks for {} project", project);
    // stub for returning all locks
    return new LfsGetLocksResponse(Collections.emptyList(), null);
  }

  protected String now() {
    return ISO.print(DateTime.now().toDateTime(DateTimeZone.UTC));
  }
}
