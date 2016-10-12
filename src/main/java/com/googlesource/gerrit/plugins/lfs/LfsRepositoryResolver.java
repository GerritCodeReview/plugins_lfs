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
  private final LfsRepositoriesCache cache;
  private final LfsBackendConfig defaultBackend;
  private final Map<String, LfsBackendConfig> backends;

  @Inject
  LfsRepositoryResolver(LfsRepositoriesCache fsRepositories,
      LfsConfigurationFactory configFactory) {
    this.cache = fsRepositories;

    LfsGlobalConfig config = configFactory.getGlobalConfig();
    this.defaultBackend = config.getDefaultBackend();
    this.backends = config.getBackends();
  }

  public LargeFileRepository get(Project.NameKey project, String backendName)
      throws LfsRepositoryNotFound {
    LfsBackendConfig config = defaultBackend;
    if (!Strings.isNullOrEmpty(backendName)) {
      config = backends.get(backendName);
      if (config == null) {
        throw new LfsRepositoryNotFound(project.get());
      }
    }

    LargeFileRepository repository;
    switch (config.type) {
      case FS:
        repository = cache.get(config);
        if (repository instanceof LocalLargeFileRepository) {
          return repository;
        }
        break;

      case S3:
        repository = cache.get(config);
        if (repository instanceof S3LargeFileRepository) {
          return repository;
        }
        break;

      default:
          throw new LfsRepositoryNotFound(project.get());
    }

    throw new LfsRepositoryNotFound(
        String.format("LFS repository for project %s was reconfigured from"
            + " %s to %s type", project, repository.getClass().getName(),
            config.type));
  }
}
