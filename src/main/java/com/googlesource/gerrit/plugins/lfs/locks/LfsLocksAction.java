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

import static com.google.gerrit.extensions.client.ProjectState.HIDDEN;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.eclipse.jgit.util.HttpSupport.HDR_AUTHORIZATION;

import com.google.common.base.Strings;
import com.google.gerrit.common.ProjectUtil;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.server.project.ProjectState;
import com.googlesource.gerrit.plugins.lfs.LfsAuthUserProvider;
import com.googlesource.gerrit.plugins.lfs.locks.LfsLocksHandler.LfsLockExistsException;
import java.io.IOException;
import org.eclipse.jgit.lfs.errors.LfsException;
import org.eclipse.jgit.lfs.errors.LfsRepositoryNotFound;
import org.eclipse.jgit.lfs.errors.LfsUnauthorized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class LfsLocksAction {
  interface Factory<T extends LfsLocksAction> {
    T create(LfsLocksContext context);
  }

  private static final Logger log = LoggerFactory.getLogger(LfsLocksAction.class);
  /** Git LFS client uses 'upload' operation to authorize SSH Lock requests */
  private static final String LFS_LOCKING_OPERATION = "upload";

  protected final ProjectCache projectCache;
  protected final LfsAuthUserProvider userProvider;
  protected final LfsLocksHandler handler;
  protected final LfsLocksContext context;

  protected LfsLocksAction(
      ProjectCache projectCache,
      LfsAuthUserProvider userProvider,
      LfsLocksHandler handler,
      LfsLocksContext context) {
    this.projectCache = projectCache;
    this.userProvider = userProvider;
    this.handler = handler;
    this.context = context;
  }

  public void run() throws IOException {
    try {
      String name = getProjectName();
      ProjectState project = getProject(name);
      CurrentUser user = getUser(name);
      ProjectControl control = project.controlFor(user);
      authorizeUser(control);
      doRun(project, user);
    } catch (LfsUnauthorized e) {
      context.sendError(SC_UNAUTHORIZED, e.getMessage());
    } catch (LfsRepositoryNotFound e) {
      context.sendError(SC_NOT_FOUND, e.getMessage());
    } catch (LfsLockExistsException e) {
      context.sendError(SC_CONFLICT, e.error);
    } catch (LfsException e) {
      context.sendError(SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  protected abstract String getProjectName() throws LfsException;

  protected abstract void authorizeUser(ProjectControl control) throws LfsUnauthorized;

  protected abstract void doRun(ProjectState project, CurrentUser user)
      throws LfsException, IOException;

  protected ProjectState getProject(String name) throws LfsRepositoryNotFound {
    Project.NameKey project = Project.NameKey.parse(ProjectUtil.stripGitSuffix(name));
    ProjectState state = projectCache.get(project);
    if (state == null || state.getProject().getState() == HIDDEN) {
      throw new LfsRepositoryNotFound(project.get());
    }
    return state;
  }

  protected CurrentUser getUser(String project) {
    return userProvider.getUser(
        context.getHeader(HDR_AUTHORIZATION), project, LFS_LOCKING_OPERATION);
  }

  protected void throwUnauthorizedOp(String op, ProjectControl control) throws LfsUnauthorized {
    String project = control.getProject().getName();
    String userName =
        Strings.isNullOrEmpty(control.getUser().getUserName())
            ? "anonymous"
            : control.getUser().getUserName();
    log.debug(
        String.format(
            "operation %s unauthorized for user %s on project %s", op, userName, project));
    throw new LfsUnauthorized(op, project);
  }
}
