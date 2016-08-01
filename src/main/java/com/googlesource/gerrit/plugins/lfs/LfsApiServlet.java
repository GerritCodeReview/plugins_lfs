//Copyright (C) 2016 The Android Open Source Project
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.googlesource.gerrit.plugins.lfs;

import static com.google.gerrit.extensions.client.ProjectState.HIDDEN;
import static com.google.gerrit.extensions.client.ProjectState.READ_ONLY;
import static com.google.gerrit.httpd.plugins.LfsPluginServlet.URL_REGEX;

import com.google.gerrit.common.ProjectUtil;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;

import org.eclipse.jgit.lfs.errors.LfsException;
import org.eclipse.jgit.lfs.errors.LfsRepositoryNotFound;
import org.eclipse.jgit.lfs.errors.LfsRepositoryReadOnly;
import org.eclipse.jgit.lfs.errors.LfsValidationError;
import org.eclipse.jgit.lfs.server.LargeFileRepository;
import org.eclipse.jgit.lfs.server.LfsObject;
import org.eclipse.jgit.lfs.server.LfsProtocolServlet;
import org.eclipse.jgit.lib.Config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LfsApiServlet extends LfsProtocolServlet {
  private static final long serialVersionUID = 1L;
  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  private final String pluginName;
  private final PluginConfigFactory pluginConfigFactory;
  private final ProjectCache projectCache;

  protected LfsApiServlet(@PluginName String pluginName,
      PluginConfigFactory pluginConfigFactory,
      ProjectCache projectCache) {
    this.pluginName = pluginName;
    this.pluginConfigFactory = pluginConfigFactory;
    this.projectCache = projectCache;
  }

  protected abstract LargeFileRepository getRepository();

  @Override
  protected LargeFileRepository getLargeFileRepository(
      LfsRequest request, String path) throws LfsException {
    String pathInfo = path.startsWith("/") ? path : "/" + path;
    Matcher matcher = URL_PATTERN.matcher(pathInfo);
    if (!matcher.matches()) {
      return null;
    }
    Project.NameKey project = Project.NameKey.parse(
        ProjectUtil.stripGitSuffix(matcher.group(1)));
    ProjectState state = projectCache.get(project);

    if (state == null || state.getProject().getState() == HIDDEN) {
      throw new LfsRepositoryNotFound(project.get());
    }

    if (request.getOperation().equals("upload")
        && state.getProject().getState() == READ_ONLY) {
      throw new LfsRepositoryReadOnly(project.get());
    }

    Config config = pluginConfigFactory.getProjectPluginConfigWithInheritance(
        state, pluginName);

    // Only accept requests for projects where LFS is enabled
    if (!config.getBoolean("lfs", "enabled", false)) {
      return null;
    }

    if (request.getOperation().equals("upload")) {
      // Check object sizes against limit, if configured
      long maxObjectSize = config.getLong("lfs", "maxObjectSize", 0);
      if (maxObjectSize > 0) {
        for (LfsObject object : request.getObjects()) {
          if (object.getSize() > maxObjectSize) {
            throw new LfsValidationError("size of object " + object.getOid()
                + " exceeds limit");
          }
        }
      }
    }

    return getRepository();
  }
}
