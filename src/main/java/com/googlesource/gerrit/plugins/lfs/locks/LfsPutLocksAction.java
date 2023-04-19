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

import static com.google.gerrit.extensions.api.lfs.LfsDefinitions.LFS_URL_REGEX_TEMPLATE;
import static com.google.gerrit.extensions.api.lfs.LfsDefinitions.LFS_VERIFICATION_PATH;
import static com.google.gerrit.server.permissions.ProjectPermission.PUSH_AT_LEAST_ONE_REF;
import static com.googlesource.gerrit.plugins.lfs.locks.LfsGetLocksAction.LFS_LOCKS_URL_PATTERN;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.permissions.PermissionBackend;
import com.google.gerrit.server.permissions.PermissionBackend.ForProject;
import com.google.gerrit.server.permissions.PermissionBackendException;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.lfs.auth.LfsAuthUserProvider;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.lfs.errors.LfsException;

public class LfsPutLocksAction extends LfsLocksAction {
  interface Factory extends LfsLocksAction.Factory<LfsPutLocksAction> {}

  private static final Pattern LFS_VERIFICATION_URL_PATTERN =
      Pattern.compile(String.format(LFS_URL_REGEX_TEMPLATE, LFS_VERIFICATION_PATH));

  protected LockAction action;

  @Inject
  LfsPutLocksAction(
      PermissionBackend permissionBackend,
      ProjectCache projectCache,
      LfsAuthUserProvider userProvider,
      LfsLocksHandler handler,
      @Assisted LfsLocksContext context) {
    super(permissionBackend, projectCache, userProvider, handler, context);
  }

  @Override
  protected String getProjectName() throws LfsException {
    Matcher matcher = LFS_LOCKS_URL_PATTERN.matcher(context.path);
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

    matcher = LFS_VERIFICATION_URL_PATTERN.matcher(context.path);
    if (matcher.matches()) {
      action = new VerifyLock();
      return matcher.group(1);
    }

    throw new LfsException(String.format("Unsupported path %s was provided", context.path));
  }

  @Override
  protected void authorizeUser(ForProject project)
      throws AuthException, PermissionBackendException {
    // all operations require push permission
    project.check(PUSH_AT_LEAST_ONE_REF);
  }

  @Override
  protected String getAction() {
    return action.getName();
  }

  @Override
  protected String getLockingOperation() {
    /** Git LFS client uses 'upload' operation to authorize writing lock requests */
    return "upload";
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
      LfsLockResponse lock = handler.createLock(project.getProject().getNameKey(), user, input);
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
      LfsLockResponse lock =
          handler.deleteLock(project.getProject().getNameKey(), user, lockId, input);
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
      context.sendResponse(handler.verifyLocks(project.getProject().getNameKey(), user));
    }
  }
}
