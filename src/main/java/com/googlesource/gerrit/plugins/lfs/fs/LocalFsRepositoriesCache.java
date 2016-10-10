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

import static com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository.DEFAULT;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Singleton;

@Singleton
public class LocalFsRepositoriesCache {
  private final Cache<String, LocalLargeFileRepository> repositories;

  LocalFsRepositoriesCache() {
    this.repositories = CacheBuilder.newBuilder().build();
  }

  public LocalLargeFileRepository getRepository(String name) {
    if (Strings.isNullOrEmpty(name)) {
      return repositories.getIfPresent(DEFAULT);
    }

    return repositories.getIfPresent(name);
  }

  public void putRepository(String name, LocalLargeFileRepository repository) {
    repositories.put(name, repository);
  }
}
