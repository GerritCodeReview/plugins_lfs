// Copyright (C) 2016 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.lfs;

import static com.google.gerrit.extensions.client.ProjectState.HIDDEN;
import static com.google.gerrit.extensions.client.ProjectState.READ_ONLY;
import static com.google.gerrit.httpd.plugins.LfsPluginServlet.URL_REGEX;

import com.google.gerrit.common.ProjectUtil;
import com.google.gerrit.common.data.Capable;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.lfs.errors.LfsException;
import org.eclipse.jgit.lfs.errors.LfsRepositoryNotFound;
import org.eclipse.jgit.lfs.errors.LfsRepositoryReadOnly;
import org.eclipse.jgit.lfs.errors.LfsUnauthorized;
import org.eclipse.jgit.lfs.errors.LfsUnavailable;
import org.eclipse.jgit.lfs.errors.LfsValidationError;
import org.eclipse.jgit.lfs.server.LargeFileRepository;
import org.eclipse.jgit.lfs.server.LfsObject;
import org.eclipse.jgit.lfs.server.LfsGerritProtocolServlet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class LfsApiServlet extends LfsGerritProtocolServlet {
  private static final long serialVersionUID = 1L;
  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
  private static final String UPLOAD = "upload";

  private final ProjectCache projectCache;
  private final LfsConfigurationFactory lfsConfigFactory;
  private final LfsRepositoryResolver repoResolver;
  private final LfsAuthUserProvider userProvider;

  @Inject
  LfsApiServlet(ProjectCache projectCache,
      LfsConfigurationFactory lfsConfigFactory,
      LfsRepositoryResolver repoResolver,
      LfsAuthUserProvider userProvider) {
    this.projectCache = projectCache;
    this.lfsConfigFactory = lfsConfigFactory;
    this.repoResolver = repoResolver;
    this.userProvider = userProvider;
  }

  @Override
  protected LargeFileRepository getLargeFileRepository(
      LfsRequest request, String path, LfsGerritProtocolServlet.AuthInfo auth)
          throws LfsException {
    String pathInfo = path.startsWith("/") ? path : "/" + path;
    Matcher matcher = URL_PATTERN.matcher(pathInfo);
    if (!matcher.matches()) {
      throw new LfsException("no repository at " + pathInfo);
    }
    Project.NameKey project = Project.NameKey.parse(
        ProjectUtil.stripGitSuffix(matcher.group(1)));
    ProjectState state = projectCache.get(project);

    authorizeUser(userProvider.getUser(auth.auth, auth.cs),
        state, request.getOperation());

    if (state == null || state.getProject().getState() == HIDDEN) {
      throw new LfsRepositoryNotFound(project.get());
    }

    if (request.getOperation().equals(UPLOAD)
        && state.getProject().getState() == READ_ONLY) {
      throw new LfsRepositoryReadOnly(project.get());
    }

    LfsProjectConfigSection config =
        lfsConfigFactory.getProjectsConfig().getForProject(project);
    // Only accept requests for projects where LFS is enabled.
    // No config means we default to "not enabled".
    if (config != null && config.isEnabled()) {
      // For uploads, check object sizes against limit if configured
      if (request.getOperation().equals(UPLOAD)) {
        if (config.isReadOnly()) {
          throw new LfsRepositoryReadOnly(project.get());
        }

        long maxObjectSize = config.getMaxObjectSize();
        if (maxObjectSize > 0) {
          for (LfsObject object : request.getObjects()) {
            if (object.getSize() > maxObjectSize) {
              throw new LfsValidationError(String.format(
                  "size of object %s (%d bytes) exceeds limit (%d bytes)",
                  object.getOid(), object.getSize(), maxObjectSize));
            }
          }
        }
      }

      return repoResolver.get(project, config.getBackend());
    }

    throw new LfsUnavailable(project.get());
  }

  private void authorizeUser(CurrentUser user, ProjectState state,
      String operation) throws LfsUnauthorized {
    ProjectControl control = state.controlFor(user);
    switch(operation) {
      case "download":
        if (control.isReadable()) {
          return;
        }
        break;

      case UPLOAD:
        if (Capable.OK == control.canPushToAtLeastOneRef()) {
          return;
        }
        break;
    }

    throw new LfsUnauthorized(
        String.format("User %s is not authorized to perform %s operation",
            user.getUserName(), operation));
  }
}
