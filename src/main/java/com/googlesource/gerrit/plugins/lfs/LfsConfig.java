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

import com.google.common.collect.ImmutableList;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.project.ProjectCache;
import com.google.inject.Inject;

import org.eclipse.jgit.lib.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LfsConfig {
  private final String pluginName;
  private final ProjectCache projectCache;
  private final Config config;

  @Inject
  LfsConfig(@PluginName String pluginName,
      ProjectCache projectCache,
      PluginConfigFactory configFactory) {
    this.pluginName = pluginName;
    this.projectCache = projectCache;
    this.config = configFactory.getGlobalPluginConfig(pluginName);
  }

  public LfsBackend getBackend() {
    return config.getEnum("storage", null, "backend", LfsBackend.FS);
  }

  public Config getConfig() {
    return config;
  }

  public List<LfsConfigSection> getConfigSections() {
    Config cfg =
        projectCache.getAllProjects().getConfig(pluginName + ".config").get();
    Set<String> namespaces = cfg.getSubsections(LfsConfigSection.LFS);
    if (!namespaces.isEmpty()) {
      ArrayList<LfsConfigSection> result = new ArrayList<>(namespaces.size());
      for (String n : namespaces) {
        result.add(new LfsConfigSection(cfg, n));
      }
      return result;
    }
    return ImmutableList.of();
  }

  public LfsConfigSection getForProject(Project.NameKey project) {
    Config cfg =
        projectCache.getAllProjects().getConfig(pluginName + ".config").get();
    Set<String> namespaces = cfg.getSubsections(LfsConfigSection.LFS);
    String p = project.get();
    for (String n : namespaces) {
      if ("?/*".equals(n) || n.endsWith("/?/*")) {
        String prefix = n.substring(0, n.length() - 3);
        Matcher m = Pattern.compile("^" + prefix + "([^/]+)/.*$").matcher(p);
        if (m.matches()) {
          return new LfsConfigSection(cfg, n);
        }
      } else if (n.endsWith("/*")) {
        if (p.startsWith(n.substring(0, n.length() - 1))) {
          return new LfsConfigSection(cfg, n);
        }
      } else if (n.startsWith("^")) {
        if (p.matches(n.substring(1))) {
          return new LfsConfigSection(cfg, n);
        }
      } else if (p.equals(n)) {
        return new LfsConfigSection(cfg, n);
      }
    }
    return null;
  }
}
