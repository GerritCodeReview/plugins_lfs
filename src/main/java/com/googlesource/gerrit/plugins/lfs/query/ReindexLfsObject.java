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

package com.googlesource.gerrit.plugins.lfs.query;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.events.LfsData;
import com.googlesource.gerrit.plugins.lfs.index.LfsObjectsIndex;
import com.googlesource.gerrit.plugins.lfs.index.LfsObjectsIndexCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class ReindexLfsObject {
  private static final Logger log =
      LoggerFactory.getLogger(ReindexLfsObject.class);

  private final Provider<LfsObjectsIndexCollection> indexCollection;
  private final Provider<InternalLfsQuery> queryProvider;
  private final LoadingCache<String, Object> locks;

  @Inject
  ReindexLfsObject(Provider<LfsObjectsIndexCollection> indexCollection,
      Provider<InternalLfsQuery> queryProvider) {
    this.indexCollection = indexCollection;
    this.queryProvider = queryProvider;
    this.locks = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build(new CacheLoader<String, Object>() {
          @Override
          public Object load(String key) throws Exception {
            return null;
          }
        });
  }

  public void reindex(List<LfsData> lfs) {
    if (lfs.isEmpty()) {
      return;
    }

    if (indexCollection.get().getSearchIndex() == null) {
      log.info("Search index is not ready");
      return;
    }
    if (indexCollection.get().getWriteIndexes().isEmpty()) {
      log.info("Write index is not ready");
      return;
    }

    for (LfsData data : lfs) {
      updateData(data);
    }
  }

  private void updateData(LfsData data) {
    // synchronize on LfsData key so that there is no chance that it
    // gets modified concurrently and as a result invalid  list of projects gets returned
    // TODO: better solution would be to try to obtain lock and if it is not possible
    // schedule update for later so that changes have a chance to be propagated
    // to search index
    Object lock = locks.getUnchecked(data.key);
    synchronized (lock) {
      try {
        LfsData toWrite;
        List<LfsData> list = queryProvider.get().byKey(data.key);
        if (list.isEmpty()) {
          toWrite = data;
        } else {
          LfsData.Builder builder = new LfsData.Builder().withData(data);
          ImmutableSet.Builder<String> projects = ImmutableSet.builder();
          toWrite = builder.withProjects(projects.addAll(data.projects)
              .addAll(list.get(0).projects).build()).build();
        }

        for (LfsObjectsIndex index : indexCollection.get().getWriteIndexes()) {
          index.replace(toWrite);
        }
      } catch (OrmException | IOException e) {
        log.error("Updating data for {} LFS object failed", data.key, e);
      }
    }
  }
}
