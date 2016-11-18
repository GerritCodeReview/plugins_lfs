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

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.extensions.webui.JavaScriptPlugin;
import com.google.gerrit.extensions.webui.WebUiPlugin;
import com.google.gerrit.httpd.plugins.HttpPluginModule;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository;
import com.googlesource.gerrit.plugins.lfs.s3.S3LargeFileRepository;

import org.eclipse.jgit.lfs.server.fs.LfsFsContentServlet;

import java.util.Map;

public class HttpModule extends HttpPluginModule {
  private final LocalLargeFileRepository.Factory fsRepoFactory;
  private final S3LargeFileRepository.Factory s3RepoFactory;
  private final LfsRepositoriesCache cache;
  private final LfsFsContentServlet.Factory fsServletFactory;
  private final LfsBackend defaultBackend;
  private final Map<String, LfsBackend> backends;

  @Inject
  HttpModule(LocalLargeFileRepository.Factory fsRepoFactory,
      S3LargeFileRepository.Factory s3RepoFactory,
      LfsRepositoriesCache cache,
      LfsFsContentServlet.Factory fsServletFactory,
      LfsConfigurationFactory configFactory) {
    this.fsRepoFactory = fsRepoFactory;
    this.s3RepoFactory = s3RepoFactory;
    this.cache = cache;
    this.fsServletFactory = fsServletFactory;

    LfsGlobalConfig config = configFactory.getGlobalConfig();
    this.defaultBackend = config.getDefaultBackend();
    this.backends = config.getBackends();
  }

  @Override
  protected void configureServlets() {
    serveRegex(URL_REGEX).with(LfsApiServlet.class);
    populateRepository(defaultBackend);
    for (LfsBackend backend : backends.values()) {
      populateRepository(backend);
    }

    DynamicSet.bind(binder(), WebUiPlugin.class)
      .toInstance(new JavaScriptPlugin("lfs-project-info.js"));
  }

  private void populateRepository(LfsBackend backend) {
    switch (backend.type) {
      case FS:
        populateAndServeFsRepository(backend);
        break;

      case S3:
        populateS3Repository(backend);
        break;

      default:
        throw new IllegalArgumentException(
            String.format("Unknown repository type: %s", backend.type));
    }
  }

  private void populateS3Repository(LfsBackend backend) {
    S3LargeFileRepository repository =
        s3RepoFactory.create(backend);
    cache.put(backend, repository);
  }

  private void populateAndServeFsRepository(LfsBackend backend) {
    LocalLargeFileRepository repository =
        fsRepoFactory.create(backend);
    cache.put(backend, repository);
    serve(repository.getServletUrlPattern())
        .with(fsServletFactory.create(repository));
  }
}
