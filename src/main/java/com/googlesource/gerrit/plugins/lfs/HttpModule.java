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

import static com.google.gerrit.httpd.plugins.LfsPluginServlet.URL_REGEX;
import static com.googlesource.gerrit.plugins.lfs.LfsBackendType.FS;

import com.google.gerrit.httpd.plugins.HttpPluginModule;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.fs.LfsFsContentServlet;
import com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository;

import java.util.Map;

public class HttpModule extends HttpPluginModule {
  private final LocalLargeFileRepository.Factory fsRepoFactory;
  private final LfsRepositoriesCache cache;
  private final LfsBackend defaultBackend;
  private final Map<String, LfsBackend> backends;

  @Inject
  HttpModule(LocalLargeFileRepository.Factory fsRepoFactory,
      LfsRepositoriesCache cache,
      LfsConfigurationFactory configFactory) {
    this.fsRepoFactory = fsRepoFactory;
    this.cache = cache;

    LfsGlobalConfig config = configFactory.getGlobalConfig();
    this.defaultBackend = config.getDefaultBackend();
    this.backends = config.getBackends();
  }

  @Override
  protected void configureServlets() {
    serveRegex(URL_REGEX).with(LfsApiServlet.class);

    if (FS.equals(defaultBackend.type)) {
      LocalLargeFileRepository defRepository =
          fsRepoFactory.create(defaultBackend);
      cache.put(defaultBackend, defRepository);
      serve(defRepository.getServletRegexp())
        .with(new LfsFsContentServlet(defRepository));
    }

    for (LfsBackend backendCfg : backends.values()) {
      if (FS.equals(backendCfg.type)) {
        LocalLargeFileRepository repository =
            fsRepoFactory.create(backendCfg);
        cache.put(backendCfg, repository);
        serve(repository.getServletRegexp())
          .with(new LfsFsContentServlet(repository));
      }
    }
  }
}
