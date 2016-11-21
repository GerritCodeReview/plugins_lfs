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

package com.googlesource.gerrit.plugins.lfs.events;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gerrit.reviewdb.client.Project;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig;
import com.googlesource.gerrit.plugins.lfs.LfsProjectConfigSection;
import com.googlesource.gerrit.plugins.lfs.LfsProjectsConfig;

import java.util.Map;

@Singleton
class ProjectToBackendCache {
  private final LoadingCache<String, LfsBackend> backends;

  @Inject
  ProjectToBackendCache(Loader loader) {
    this.backends = CacheBuilder.newBuilder().build(loader);
  }

  LfsBackend get(String project) {
    return backends.getUnchecked(project);
  }

  static class Loader extends CacheLoader<String, LfsBackend> {
    private final LfsProjectsConfig projectsCfg;
    private final LfsBackend defaultBackend;
    private final Map<String, LfsBackend> backends;

    @Inject
    Loader(LfsConfigurationFactory configFactory) {
      this.projectsCfg = configFactory.getProjectsConfig();
      LfsGlobalConfig config = configFactory.getGlobalConfig();
      this.defaultBackend = config.getDefaultBackend();
      this.backends = config.getBackends();
    }

    @Override
    public LfsBackend load(String project) throws Exception {
      LfsProjectConfigSection cfg =
          projectsCfg.getForProject(new Project.NameKey(project));
      String backend = cfg.getBackend();
      if (Strings.isNullOrEmpty(backend)) {
        return defaultBackend;
      }
      return backends.get(backend);
    }
  }
}
