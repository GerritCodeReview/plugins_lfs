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

import static com.google.gerrit.entities.RefNames.REFS_CONFIG;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gerrit.entities.Project;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.AllProjectsName;
import com.google.gerrit.server.git.meta.VersionedMetaData;
import com.google.gerrit.server.project.ProjectCache;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Config;

/**
 * Represents the project-specific LFS configuration stored in the lfs.config file on the
 * refs/meta/config branch of All-Projects.
 */
public class LfsProjectsConfig extends VersionedMetaData {
  private final String configFilename;
  private final ProjectCache projectCache;
  private final AllProjectsName allProjects;
  private Config projectConfig;

  public LfsProjectsConfig(
      @PluginName String pluginName, ProjectCache projectCache, AllProjectsName allProjects) {
    this.configFilename = pluginName + ".config";
    this.projectCache = projectCache;
    this.allProjects = allProjects;
    this.projectConfig = loadProjectConfig();
  }

  /** @return all the configured LFS sections. */
  public List<LfsProjectConfigSection> getConfigSections() {
    Set<String> namespaces = projectConfig.getSubsections(LfsProjectConfigSection.LFS);
    if (!namespaces.isEmpty()) {
      ArrayList<LfsProjectConfigSection> result = new ArrayList<>(namespaces.size());
      for (String n : namespaces) {
        result.add(new LfsProjectConfigSection(projectConfig, n));
      }
      return result;
    }
    return ImmutableList.of();
  }

  /**
   * Get the project-specific configuration for a project
   *
   * @param project the project.
   * @return configuration.
   */
  public LfsProjectConfigSection getForProject(Project.NameKey project) {
    Set<String> namespaces = projectConfig.getSubsections(LfsProjectConfigSection.LFS);
    String p = project.get();
    for (String n : namespaces) {
      if ("?/*".equals(n) || n.endsWith("/?/*")) {
        String prefix = n.substring(0, n.length() - 3);
        Matcher m = Pattern.compile("^" + prefix + "([^/]+)/.*$").matcher(p);
        if (m.matches()) {
          return new LfsProjectConfigSection(projectConfig, n);
        }
      } else if (n.endsWith("/*")) {
        if (p.startsWith(n.substring(0, n.length() - 1))) {
          return new LfsProjectConfigSection(projectConfig, n);
        }
      } else if (n.startsWith("^")) {
        if (p.matches(n.substring(1))) {
          return new LfsProjectConfigSection(projectConfig, n);
        }
      } else if (p.equals(n)) {
        return new LfsProjectConfigSection(projectConfig, n);
      }
    }
    return null;
  }

  /**
   * Set a new config, overriding the one that was loaded from All-Projects.
   *
   * @param cfg the new config.
   */
  public void setProjectConfig(Config cfg) {
    this.projectConfig = cfg;
  }

  private Config loadProjectConfig() {
    return projectCache.getAllProjects().getConfig(configFilename).get();
  }

  @Override
  protected String getRefName() {
    return REFS_CONFIG;
  }

  @Override
  protected void onLoad() throws IOException, ConfigInvalidException {
    loadProjectConfig();
  }

  @Override
  protected boolean onSave(CommitBuilder commit) throws IOException, ConfigInvalidException {
    if (Strings.isNullOrEmpty(commit.getMessage())) {
      commit.setMessage("Update LFS configuration\n");
    }
    saveConfig(configFilename, projectConfig);
    projectCache.evict(allProjects);
    return true;
  }
}
