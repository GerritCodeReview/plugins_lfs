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

package com.googlesource.gerrit.plugins.lfs.fs;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.LfsBackendConfig;

@Singleton
public class LocalFsRepositoriesCache {
  private final LoadingCache<LfsBackendConfig, LocalLargeFileRepository> repositories;

  @Inject
  LocalFsRepositoriesCache(LocalFsRepositoriesCache.Loader loader) {
    this.repositories = CacheBuilder.newBuilder().build(loader);
  }

  public LocalLargeFileRepository getRepository(LfsBackendConfig cfg) {
    return repositories.getUnchecked(cfg);
  }

  static class Loader extends
    CacheLoader<LfsBackendConfig, LocalLargeFileRepository> {
    private final LocalLargeFileRepository.Factory fsRepoFactory;

    @Inject
    Loader(LocalLargeFileRepository.Factory fsRepoFactory) {
      this.fsRepoFactory = fsRepoFactory;
    }

    @Override
    public LocalLargeFileRepository load(LfsBackendConfig cfg) throws Exception {
      return fsRepoFactory.create(cfg);
    }
  }
}
