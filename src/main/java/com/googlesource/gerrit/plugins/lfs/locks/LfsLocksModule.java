// Copyright (C) 2017 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.lfs.locks;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.reviewdb.client.Project;
import com.google.inject.Provides;
import java.util.concurrent.TimeUnit;

public class LfsLocksModule extends FactoryModule {
  @Override
  protected void configure() {
    factory(LfsGetLocksAction.Factory.class);
    factory(LfsPutLocksAction.Factory.class);
    factory(LfsProjectLocks.Factory.class);
  }

  @Provides
  LoadingCache<Project.NameKey, LfsProjectLocks> getProjectLocksCache(
      LfsLocksHandler.Loader loader) {
    return CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build(loader);
  }
}
