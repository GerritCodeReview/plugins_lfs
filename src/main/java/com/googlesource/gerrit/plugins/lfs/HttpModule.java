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
import static com.googlesource.gerrit.plugins.lfs.LfsBackend.FS;

import com.google.gerrit.httpd.plugins.HttpPluginModule;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.fs.LfsFsContentServlet;
import com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository;

import java.util.Map;

public class HttpModule extends HttpPluginModule {
  private final LocalLargeFileRepository.Factory fsRepoFactory;
  private final LfsBackendConfig defBackendCfg;
  private final Map<String, LfsBackendConfig> backends;

  @Inject
  HttpModule(LocalLargeFileRepository.Factory fsRepoFactory,
      LfsConfigurationFactory configFactory) {
    this.fsRepoFactory = fsRepoFactory;

    LfsGlobalConfig config = configFactory.getGlobalConfig();
    this.defBackendCfg = config.getDefaultBackend();
    this.backends = config.getBackends();
  }

  @Override
  protected void configureServlets() {
    serveRegex(URL_REGEX).with(LfsApiServlet.class);

    if (FS.equals(defBackendCfg.type)) {
      LocalLargeFileRepository defBackend = fsRepoFactory.create(defBackendCfg);
      serve(defBackend.getServletRegexp()).with(new LfsFsContentServlet(defBackend));
    }

    for (LfsBackendConfig backendCfg : backends.values()) {
      if (FS.equals(backendCfg.type)) {
        LocalLargeFileRepository backend = fsRepoFactory.create(backendCfg);
        serve(backend.getServletRegexp()).with(new LfsFsContentServlet(backend));
      }
    }
  }
}
