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

import org.eclipse.jgit.lfs.server.LargeFileRepository;
import org.eclipse.jgit.lfs.server.LfsProtocolServlet;

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

  @Override
  protected abstract LargeFileRepository getLargeFileRepository();

  @Override
  protected boolean accept(LfsRequest request, String path) {
    String pathInfo = path.startsWith("/") ? path : "/" + path;
    Matcher matcher = URL_PATTERN.matcher(pathInfo);
    if (!matcher.matches()) {
      return false;
    }
    Project.NameKey project = Project.NameKey.parse(
        ProjectUtil.stripGitSuffix(matcher.group(1)));
    ProjectState state = projectCache.get(project);

    // Reject all requests for hidden projects
    if (state == null || state.getProject().getState() == HIDDEN) {
      return false;
    }

    // Accept download and verify requests
    if (!request.getOperation().equals("upload")) {
      return true;
    }

    // Reject upload requests for read-only projects
    if (state.getProject().getState() == READ_ONLY) {
      return false;
    }

    // Accept requests for projects where LFS is enabled
    return pluginConfigFactory.getProjectPluginConfigWithInheritance(
        state, pluginName).getBoolean("lfs", "enabled", true);
  }
}
