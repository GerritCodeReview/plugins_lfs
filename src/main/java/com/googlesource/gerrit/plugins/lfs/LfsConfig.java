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

import static com.google.gerrit.reviewdb.client.RefNames.REFS_CONFIG;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.git.VersionedMetaData;
import com.google.gerrit.server.project.ProjectCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LfsConfig extends VersionedMetaData {
  private final String configFilename;
  private final ProjectCache projectCache;
  private final Config globalConfig;
  private final Project.NameKey projectName;
  private Config projectConfig;

  @Singleton
  public static class Factory {
    final String pluginName;
    final ProjectCache projectCache;
    final PluginConfigFactory configFactory;
    final Project.NameKey projectName;

    @Inject
    public Factory(@PluginName String pluginName,
        ProjectCache projectCache,
        PluginConfigFactory configFactory,
        Project.NameKey projectName) {
      this.pluginName = pluginName;
      this.projectCache = projectCache;
      this.configFactory = configFactory;
      this.projectName = projectName;
    }

    public LfsConfig create() {
      return new LfsConfig(pluginName, projectCache, configFactory);
    }

    public LfsConfig create(Project.NameKey projectName) {
      return new LfsConfig(pluginName, projectCache, configFactory, projectName);
    }
  }

  private LfsConfig(@PluginName String pluginName,
      ProjectCache projectCache,
      PluginConfigFactory configFactory,
      Project.NameKey projectName) {
    this.configFilename = pluginName + ".config";
    this.projectCache = projectCache;
    this.globalConfig = configFactory.getGlobalPluginConfig(pluginName);
    this.projectConfig = loadProjectConfig();
    this.projectName = projectName;
  }

  private LfsConfig(@PluginName String pluginName,
      ProjectCache projectCache,
      PluginConfigFactory configFactory) {
    this(pluginName, projectCache, configFactory, null);
  }

  public LfsBackend getBackend() {
    return globalConfig.getEnum("storage", null, "backend", LfsBackend.FS);
  }

  public Config getGlobalConfig() {
    return globalConfig;
  }

  public List<LfsConfigSection> getConfigSections() {
    Set<String> namespaces = projectConfig.getSubsections(LfsConfigSection.LFS);
    if (!namespaces.isEmpty()) {
      ArrayList<LfsConfigSection> result = new ArrayList<>(namespaces.size());
      for (String n : namespaces) {
        result.add(new LfsConfigSection(projectConfig, n));
      }
      return result;
    }
    return ImmutableList.of();
  }

  public LfsConfigSection getForProject(Project.NameKey project) {
    Set<String> namespaces = projectConfig.getSubsections(LfsConfigSection.LFS);
    String p = project.get();
    for (String n : namespaces) {
      if ("?/*".equals(n) || n.endsWith("/?/*")) {
        String prefix = n.substring(0, n.length() - 3);
        Matcher m = Pattern.compile("^" + prefix + "([^/]+)/.*$").matcher(p);
        if (m.matches()) {
          return new LfsConfigSection(projectConfig, n);
        }
      } else if (n.endsWith("/*")) {
        if (p.startsWith(n.substring(0, n.length() - 1))) {
          return new LfsConfigSection(projectConfig, n);
        }
      } else if (n.startsWith("^")) {
        if (p.matches(n.substring(1))) {
          return new LfsConfigSection(projectConfig, n);
        }
      } else if (p.equals(n)) {
        return new LfsConfigSection(projectConfig, n);
      }
    }
    return null;
  }

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
    // Do nothing. We don't need to load the project config because the
    // only call site that opens the config via VersionedMetaData is
    // going to overwrite it anyway by calling #setProjectConfig.
  }

  @Override
  protected boolean onSave(CommitBuilder commit)
      throws IOException, ConfigInvalidException {
    if (Strings.isNullOrEmpty(commit.getMessage())) {
      commit.setMessage("Update LFS configuration\n");
    }
    saveConfig(configFilename, projectConfig);
    if (projectName != null) {
      projectCache.evict(projectName);
    }
    return true;
  }
}
