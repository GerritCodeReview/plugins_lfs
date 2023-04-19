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

import static com.google.gerrit.extensions.api.lfs.LfsDefinitions.LFS_LOCKS_PATH_REGEX;
import static com.google.gerrit.extensions.api.lfs.LfsDefinitions.LFS_URL_REGEX_TEMPLATE;
import static com.google.gerrit.server.permissions.ProjectPermission.ACCESS;

import com.google.common.base.Strings;
import com.google.gerrit.entities.Project;
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

public class LfsGetLocksAction extends LfsLocksAction {
  interface Factory extends LfsLocksAction.Factory<LfsGetLocksAction> {}

  static final Pattern LFS_LOCKS_URL_PATTERN =
      Pattern.compile(String.format(LFS_URL_REGEX_TEMPLATE, LFS_LOCKS_PATH_REGEX));

  @Inject
  LfsGetLocksAction(
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
      return matcher.group(1);
    }

    throw new LfsException("no repository at " + context.path);
  }

  @Override
  protected void authorizeUser(ForProject project)
      throws AuthException, PermissionBackendException {
    project.check(ACCESS);
  }

  @Override
  protected String getAction() {
    return "list-locks";
  }

  @Override
  protected String getLockingOperation() {
    /** Git LFS client uses 'download' operation to authorize reading lock requests */
    return "download";
  }

  @Override
  protected void doRun(ProjectState project, CurrentUser user) throws LfsException, IOException {
    Project.NameKey name = project.getProject().getNameKey();
    String path = context.getParam("path");
    if (!Strings.isNullOrEmpty(path)) {
      context.sendResponse(handler.listLocksByPath(name, path));
      return;
    }

    String id = context.getParam("id");
    if (!Strings.isNullOrEmpty(id)) {
      context.sendResponse(handler.listLocksById(name, id));
      return;
    }

    context.sendResponse(handler.listLocks(name));
  }
}
