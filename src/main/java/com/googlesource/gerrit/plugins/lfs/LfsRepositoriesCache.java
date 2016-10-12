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

import static com.googlesource.gerrit.plugins.lfs.LfsBackend.DEFAULT;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository;
import com.googlesource.gerrit.plugins.lfs.s3.S3LargeFileRepository;

import org.eclipse.jgit.lfs.errors.LfsRepositoryNotFound;
import org.eclipse.jgit.lfs.server.LargeFileRepository;

@Singleton
public class LfsRepositoriesCache {
  private final LoadingCache<LfsBackend, LargeFileRepository> cache;

  @Inject
  LfsRepositoriesCache(LfsRepositoriesCache.Loader loader) {
    this.cache = CacheBuilder.newBuilder().build(loader);
  }

  public LargeFileRepository get(LfsBackend cfg) {
    return cache.getUnchecked(cfg);
  }

  public void put(LfsBackend cfg, LargeFileRepository repo) {
    cache.put(cfg, repo);
  }

  static class Loader extends
    CacheLoader<LfsBackend, LargeFileRepository> {
    private final LocalLargeFileRepository.Factory fsRepoFactory;
    private final S3LargeFileRepository.Factory s3RepoFactory;

    @Inject
    Loader(LocalLargeFileRepository.Factory fsRepoFactory,
        S3LargeFileRepository.Factory s3RepoFactory) {
      this.fsRepoFactory = fsRepoFactory;
      this.s3RepoFactory = s3RepoFactory;
    }

    @Override
    public LargeFileRepository load(LfsBackend cfg)
        throws Exception {
      switch (cfg.type) {
        case FS:
          return fsRepoFactory.create(cfg);

        case S3:
          return s3RepoFactory.create(cfg);

        default:
          throw new LfsRepositoryNotFound(
              String.format("Repository: %s, of type %s was not found",
                Strings.isNullOrEmpty(cfg.name) ? DEFAULT : cfg.name, cfg.type));
      }
    }
  }
}
