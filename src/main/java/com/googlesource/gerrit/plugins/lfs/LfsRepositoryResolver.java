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

import com.google.common.base.Strings;
import com.google.gerrit.reviewdb.client.Project;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository;
import com.googlesource.gerrit.plugins.lfs.s3.S3LargeFileRepository;

import org.eclipse.jgit.lfs.errors.LfsRepositoryNotFound;
import org.eclipse.jgit.lfs.server.LargeFileRepository;

import java.util.Map;

public class LfsRepositoryResolver {
  private final LocalLargeFileRepository.Factory fsRepoFactory;
  private final S3LargeFileRepository.Factory s3RepoFactory;
  private final LfsBackend defaultBackend;
  private final Map<String, LfsBackend> backends;

  @Inject
  LfsRepositoryResolver(LocalLargeFileRepository.Factory fsRepoFactory,
      S3LargeFileRepository.Factory s3RepoFactory,
      LfsConfigurationFactory configFactory) {
    this.fsRepoFactory = fsRepoFactory;
    this.s3RepoFactory = s3RepoFactory;

    LfsGlobalConfig config = configFactory.getGlobalConfig();
    this.defaultBackend = config.getDefaultBackend();
    this.backends = config.getBackends();
  }

  public LargeFileRepository get(Project.NameKey project, String backendName)
      throws LfsRepositoryNotFound {
    LfsBackend config;
    if (Strings.isNullOrEmpty(backendName)) {
      config = defaultBackend;
    } else {
      config = backends.get(backendName);
      if (config == null) {
        throw new LfsRepositoryNotFound(project.get());
      }
    }

    switch (config.type) {
      case FS:
        return fsRepoFactory.create(config);

      case S3:
        return s3RepoFactory.create(config);

      default:
          throw new LfsRepositoryNotFound(project.get());
    }
  }
}
