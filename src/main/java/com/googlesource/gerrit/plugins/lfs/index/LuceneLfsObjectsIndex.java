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

import static com.googlesource.gerrit.plugins.lfs.index.LfsObjectsSchemaDefinitions.LFS_OBJECTS_FORMAT;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.config.SitePaths;
import com.google.gerrit.server.index.QueryOptions;
import com.google.gerrit.server.index.Schema;
import com.google.gerrit.server.query.DataSource;
import com.google.gerrit.server.query.Predicate;
import com.google.gerrit.server.query.QueryParseException;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.googlesource.gerrit.plugins.index.LucenePluginVersionManager;
import com.googlesource.gerrit.plugins.index.PluginAbstractLuceneIndex;
import com.googlesource.gerrit.plugins.index.PluginGerritIndexWriterConfig;
import com.googlesource.gerrit.plugins.lfs.events.LfsData;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class LuceneLfsObjectsIndex
  extends PluginAbstractLuceneIndex<String, LfsData>
  implements LfsObjectsIndex {
  @Inject
  LuceneLfsObjectsIndex(PluginConfigFactory configFactory,
      SitePaths sitePaths,
      @PluginName String name,
      @Assisted Schema<LfsData> schema) throws IOException {
    super(schema, sitePaths,
        dir(String.format(LFS_OBJECTS_FORMAT, name), schema, sitePaths),
        String.format(LFS_OBJECTS_FORMAT, name), null,
        new PluginGerritIndexWriterConfig(
            configFactory.getGlobalPluginConfig(name),
            String.format(LFS_OBJECTS_FORMAT, name)),
        new SearcherFactory());
  }

  @Override
  public void replace(LfsData obj) throws IOException {
    try {
      replace(idTerm(obj), toDocument(obj, null)).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void delete(String key) throws IOException {
    try {
      delete(idTerm(key)).get();
    } catch (ExecutionException | InterruptedException e) {
      throw new IOException(e);
    }
  }

  @Override
  public DataSource<LfsData> getSource(Predicate<LfsData> p, QueryOptions opts)
      throws QueryParseException {
    return null;
  }

  private static Term idTerm(LfsData lfs) {
    return idTerm(lfs.key);
  }

  private static Term idTerm(String key) {
    return new Term(LfsFields.KEY.getName(), key);
  }

  private static Directory dir(String name, Schema<LfsData> schema, SitePaths sitePaths)
      throws IOException {
    Path indexDir =
        LucenePluginVersionManager.getDir(sitePaths, name + "_", schema);
    return FSDirectory.open(indexDir);
  }
}
