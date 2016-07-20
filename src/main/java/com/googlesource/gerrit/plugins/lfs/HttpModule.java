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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.httpd.plugins.HttpPluginModule;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.fs.LfsFsApiServlet;
import com.googlesource.gerrit.plugins.lfs.fs.LfsFsContentServlet;
import com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository;
import com.googlesource.gerrit.plugins.lfs.s3.LfsS3ApiServlet;
import com.googlesource.gerrit.plugins.lfs.s3.S3LargeFileRepository;

public class HttpModule extends HttpPluginModule {
  private final PluginConfig config;

  @Inject
  HttpModule(PluginConfigFactory config, @PluginName String pluginName) {
    this.config = config.getFromGerritConfig(pluginName);
  }

  @Override
  protected void configureServlets() {
    LfsBackend backend = config.getEnum("backend", LfsBackend.FS);
    switch (backend) {
      case FS:
        serveRegex(URL_REGEX).with(LfsFsApiServlet.class);
        bind(LocalLargeFileRepository.class);
        serve("/*").with(LfsFsContentServlet.class);
        break;
      case S3:
        serveRegex(URL_REGEX).with(LfsS3ApiServlet.class);
        bind(S3LargeFileRepository.class);
        break;
      default:
        throw new RuntimeException("Unsupported backend: " + backend);
    }
  }
}
