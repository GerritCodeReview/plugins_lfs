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

import static com.googlesource.gerrit.plugins.lfs.LfsPaths.LFS_VERIFICATION_PATH;
import static com.googlesource.gerrit.plugins.lfs.LfsPaths.URL_REGEX_TEMPLATE;
import static com.googlesource.gerrit.plugins.lfs.locks.LfsGetLocksAction.LFS_LOCKS_URL;

import com.google.common.base.Strings;
import com.google.gerrit.common.data.Capable;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.lfs.LfsAuthUserProvider;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.lfs.errors.LfsException;
import org.eclipse.jgit.lfs.errors.LfsUnauthorized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LfsPutLocksAction extends LfsLocksAction {
  interface Factory extends LfsLocksAction.Factory<LfsPutLocksAction> {}

  private static final Logger log = LoggerFactory.getLogger(LfsPutLocksAction.class);
  private static final Pattern LFS_VERIFICATION_URL =
      Pattern.compile(String.format(URL_REGEX_TEMPLATE, LFS_VERIFICATION_PATH));

  protected LockAction action;

  @Inject
  LfsPutLocksAction(
      ProjectCache projectCache,
      LfsAuthUserProvider userProvider,
      @Assisted LfsLocksContext context) {
    super(projectCache, userProvider, context);
  }

  @Override
  protected String getProjectName() throws LfsException {
    Matcher matcher = LFS_LOCKS_URL.matcher(context.path);
    if (matcher.matches()) {
      String project = matcher.group(1);
      String lockId = matcher.group(2);
      if (Strings.isNullOrEmpty(lockId)) {
        action = new CreateLock();
      } else {
        action = new DeleteLock(lockId);
      }
      return project;
    }

    matcher = LFS_VERIFICATION_URL.matcher(context.path);
    if (matcher.matches()) {
      action = new VerifyLock();
      return matcher.group(1);
    }

    throw new LfsException(String.format("Unsupported path %s was provided", context.path));
  }

  @Override
  protected void authorizeUser(ProjectControl control) throws LfsUnauthorized {
    // all operations require push permission
    if (Capable.OK != control.canPushToAtLeastOneRef()) {
      throwUnauthroizedOp(action.getName(), control, log);
    }
  }

  @Override
  protected void doRun(ProjectState project, CurrentUser user) throws LfsException, IOException {
    action.run(project, user);
  }

  private interface LockAction {
    String getName();

    void run(ProjectState project, CurrentUser user) throws LfsException, IOException;
  }

  private class CreateLock implements LockAction {
    @Override
    public String getName() {
      return "create lock";
    }

    @Override
    public void run(ProjectState project, CurrentUser user) throws LfsException, IOException {
      LfsCreateLockInput input = context.input(LfsCreateLockInput.class);
      log.debug("Create lock for {} in project {}", input.path, project);
      //TODO: this is just the method stub lock creation
      LfsLock lock =
          new LfsLock(
              "random_id",
              input.path,
              now(),
              new LfsLockOwner("Lock Owner <lock_owner@example.com>"));
      context.sendResponse(lock);
    }
  }

  private class DeleteLock implements LockAction {
    private final String lockId;

    private DeleteLock(String lockId) {
      this.lockId = lockId;
    }

    @Override
    public String getName() {
      return "delete lock";
    }

    @Override
    public void run(ProjectState project, CurrentUser user) throws LfsException, IOException {
      LfsDeleteLockInput input = context.input(LfsDeleteLockInput.class);
      log.debug(
          "Delete (-f {}) lock for {} in project {}",
          Boolean.TRUE.equals(input.force),
          lockId,
          project);
      //TODO: this is just the method stub for lock deletion
      LfsLock lock =
          new LfsLock(
              lockId,
              "some/path/to/file",
              now(),
              new LfsLockOwner("Lock Owner <lock_owner@example.com>"));
      context.sendResponse(lock);
    }
  }

  private class VerifyLock implements LockAction {
    @Override
    public String getName() {
      return "verify lock";
    }

    @Override
    public void run(ProjectState project, CurrentUser user) throws LfsException, IOException {
      log.debug("Verify list of locks for {} project", project);
      //TODO method stub for verifying locks
      context.sendResponse(
          new LfsVerifyLocksResponse(Collections.emptyList(), Collections.emptyList(), null));
    }
  }
}
