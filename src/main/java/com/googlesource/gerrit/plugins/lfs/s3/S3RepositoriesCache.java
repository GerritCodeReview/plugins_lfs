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

package com.googlesource.gerrit.plugins.lfs.s3;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.LfsBackendConfig;

@Singleton
public class S3RepositoriesCache {
  private final
    LoadingCache<LfsBackendConfig, S3LargeFileRepository> repositories;

  @Inject
  S3RepositoriesCache(S3RepositoriesCache.Loader loader) {
    repositories = CacheBuilder.newBuilder().build(loader);
  }

  public S3LargeFileRepository getRepository(LfsBackendConfig cfg) {
    return repositories.getUnchecked(cfg);
  }

  static class Loader extends
    CacheLoader<LfsBackendConfig, S3LargeFileRepository> {
    private final S3LargeFileRepository.Factory s3RepoFactory;

    @Inject
    Loader(S3LargeFileRepository.Factory s3RepoFactory) {
      this.s3RepoFactory = s3RepoFactory;
    }

    @Override
    public S3LargeFileRepository load(LfsBackendConfig cfg) throws Exception {
      return s3RepoFactory.create(cfg);
    }
  }
}
