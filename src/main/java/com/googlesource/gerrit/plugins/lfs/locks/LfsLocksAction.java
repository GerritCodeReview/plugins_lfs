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

import com.google.common.flogger.FluentLogger;
import com.google.gerrit.entities.Project;
import com.google.gerrit.exceptions.StorageException;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.ProjectUtil;
import com.google.gerrit.server.permissions.PermissionBackend;
import com.google.gerrit.server.permissions.PermissionBackend.ForProject;
import com.google.gerrit.server.permissions.PermissionBackendException;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;
import com.googlesource.gerrit.plugins.lfs.auth.LfsAuthUserProvider;
import com.googlesource.gerrit.plugins.lfs.locks.LfsLocksHandler.LfsLockExistsException;
import java.io.IOException;
import java.util.Optional;
import org.eclipse.jgit.lfs.errors.LfsException;
import org.eclipse.jgit.lfs.errors.LfsRepositoryNotFound;
import org.eclipse.jgit.lfs.errors.LfsUnauthorized;

abstract class LfsLocksAction {
  interface Factory<T extends LfsLocksAction> {
    T create(LfsLocksContext context);
  }

  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  protected final ProjectCache projectCache;
  protected final LfsAuthUserProvider userProvider;
  protected final LfsLocksHandler handler;
  protected final LfsLocksContext context;
  protected final PermissionBackend permissionBackend;

  protected LfsLocksAction(
      PermissionBackend permissionBackend,
      ProjectCache projectCache,
      LfsAuthUserProvider userProvider,
      LfsLocksHandler handler,
      LfsLocksContext context) {
    this.permissionBackend = permissionBackend;
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
      Optional<ProjectState> state = projectCache.get(project.getNameKey());
      if (!state.isPresent()) {
        throw new LfsRepositoryNotFound(project.getNameKey().get());
      }
      try {
        authorizeUser(permissionBackend.user(user).project(state.get().getNameKey()));
      } catch (AuthException | PermissionBackendException e) {
        throwUnauthorizedOp(getAction(), project, user);
      }
      doRun(project, user);
    } catch (LfsUnauthorized e) {
      context.sendError(SC_UNAUTHORIZED, e.getMessage());
    } catch (LfsRepositoryNotFound e) {
      context.sendError(SC_NOT_FOUND, e.getMessage());
    } catch (LfsLockExistsException e) {
      context.sendError(SC_CONFLICT, e.error);
    } catch (LfsException | StorageException e) {
      context.sendError(SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  protected abstract String getProjectName() throws LfsException;

  protected abstract String getAction();

  protected abstract String getLockingOperation();

  protected abstract void authorizeUser(ForProject project)
      throws AuthException, PermissionBackendException;

  protected abstract void doRun(ProjectState project, CurrentUser user)
      throws LfsException, IOException;

  protected ProjectState getProject(String name) throws LfsRepositoryNotFound {
    Project.NameKey project = Project.nameKey(ProjectUtil.stripGitSuffix(name));
    Optional<ProjectState> state = projectCache.get(project);
    if (!state.isPresent() || state.get().getProject().getState() == HIDDEN) {
      throw new LfsRepositoryNotFound(project.get());
    }
    return state.get();
  }

  protected CurrentUser getUser(String project) {
    return userProvider.getUser(
        context.getHeader(HDR_AUTHORIZATION), project, getLockingOperation());
  }

  private void throwUnauthorizedOp(String op, ProjectState state, CurrentUser user)
      throws LfsUnauthorized {
    String project = state.getProject().getName();
    String userName = user.getUserName().orElse("anonymous");
    log.atFine().log("operation %s unauthorized for user %s on project %s", op, userName, project);
    throw new LfsUnauthorized(op, project);
  }
}
