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

package com.googlesource.gerrit.plugins.lfs.index;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.server.index.IndexDefinition;
import com.google.gerrit.server.index.SchemaDefinitions;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import com.googlesource.gerrit.plugins.index.LucenePluginModule;

public class LuceneLfsModule extends LucenePluginModule {
  public static final String LFS_INDEXES = "lfs";

  public LuceneLfsModule() {
    super(LuceneLfsManager.class, LfsIndexesProvider.class,
        LFS_INDEXES);
  }

  @Override
  protected void configureIndexes() {
    install(new FactoryModuleBuilder()
      .implement(LfsObjectsIndex.class, LuceneLfsObjectsIndex.class)
      .build(LfsObjectsIndex.Factory.class));

    bind(LfsObjectsSchemaDefinitions.class).in(Singleton.class);
    bind(LfsObjectsIndexCollection.class);
    listener().to(LfsObjectsIndexCollection.class);
    factory(LfsObjectsIndexer.Factory.class);
  }

  @Provides
  @Singleton
  LfsObjectsIndexer getLfsRepositoryIndexer(
      LfsObjectsIndexer.Factory factory,
      LfsObjectsIndexCollection indexes) {
    return factory.create(indexes);
  }

  static class LfsIndexesProvider extends IndexesProvider {
    @Inject
    LfsIndexesProvider(LfsObjectsSchemaDefinitions def,
        LfsObjectsIndexDefinition lfsIndex) {
      super(ImmutableList.<SchemaDefinitions<?>> of(def),
          ImmutableList.<IndexDefinition<?, ?, ?>> of(lfsIndex));
    }
  }
}