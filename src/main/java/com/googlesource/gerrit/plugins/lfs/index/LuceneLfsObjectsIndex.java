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

import static com.googlesource.gerrit.plugins.lfs.index.LfsFields.KEY;
import static com.googlesource.gerrit.plugins.lfs.index.LfsFields.PROJECT;
import static com.googlesource.gerrit.plugins.lfs.index.LfsFields.REQUIRED_FIELDS;
import static com.googlesource.gerrit.plugins.lfs.index.LfsFields.SIZE;
import static com.googlesource.gerrit.plugins.lfs.index.LfsObjectsSchemaDefinitions.LFS_OBJECTS_FORMAT;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.lucene.QueryBuilder;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.config.SitePaths;
import com.google.gerrit.server.index.QueryOptions;
import com.google.gerrit.server.index.Schema;
import com.google.gerrit.server.query.DataSource;
import com.google.gerrit.server.query.Predicate;
import com.google.gerrit.server.query.QueryParseException;
import com.google.gwtorm.server.OrmException;
import com.google.gwtorm.server.ResultSet;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.googlesource.gerrit.plugins.index.LucenePluginVersionManager;
import com.googlesource.gerrit.plugins.index.PluginAbstractLuceneIndex;
import com.googlesource.gerrit.plugins.index.PluginGerritIndexWriterConfig;
import com.googlesource.gerrit.plugins.lfs.events.LfsData;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LuceneLfsObjectsIndex
  extends PluginAbstractLuceneIndex<String, LfsData>
  implements LfsObjectsIndex {
  private static final Logger log =
      LoggerFactory.getLogger(LuceneLfsObjectsIndex.class);
  private static final String ID_SORT_FIELD = sortFieldName(KEY);

  private final QueryBuilder<LfsData> queryBuilder;

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
    PluginGerritIndexWriterConfig writerConfig = new PluginGerritIndexWriterConfig(
        configFactory.getGlobalPluginConfig(name),
        String.format(LFS_OBJECTS_FORMAT, name));
    queryBuilder = new QueryBuilder<>(schema, writerConfig.getAnalyzer());
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
    return new QuerySource(opts,
        queryBuilder.toQuery(p),
        new Sort(new SortField(ID_SORT_FIELD, SortField.Type.STRING)));
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

  private class QuerySource implements DataSource<LfsData> {
    private final QueryOptions opts;
    private final Query query;
    private final Sort sort;

    private QuerySource(QueryOptions opts, Query query, Sort sort) {
      this.opts = opts;
      this.query = query;
      this.sort = sort;
    }

    @Override
    public int getCardinality() {
      return 10;
    }

    @Override
    public ResultSet<LfsData> read() throws OrmException {
      IndexSearcher searcher = null;
      try {
        searcher = acquire();
        int realLimit = opts.start() + opts.limit();
        TopFieldDocs docs = searcher.search(query, realLimit, sort);
        List<LfsData> result = new ArrayList<>(docs.scoreDocs.length);
        for (int i = opts.start(); i < docs.scoreDocs.length; i++) {
          ScoreDoc sd = docs.scoreDocs[i];
          Document doc = searcher.doc(sd.doc, REQUIRED_FIELDS);
          result.add(toFsData(doc));
        }
        final List<LfsData> r = Collections.unmodifiableList(result);
        return new ResultSet<LfsData>() {
          @Override
          public Iterator<LfsData> iterator() {
            return r.iterator();
          }

          @Override
          public List<LfsData> toList() {
            return r;
          }

          @Override
          public void close() {
            // Do nothing.
          }
        };
      } catch (IOException e) {
        throw new OrmException(e);
      } finally {
        if (searcher != null) {
          try {
            release(searcher);
          } catch (IOException e) {
            log.warn("cannot release Lucene searcher", e);
          }
        }
      }
    }

    private LfsData toFsData(Document doc) {
      LfsData.Builder builder = new LfsData.Builder();
      builder.withKey(doc.getField(KEY.getName()).stringValue());
      builder.withSize(doc.getField(SIZE.getName()).numericValue().longValue());
      List<IndexableField> projects = fields(doc, PROJECT.getName());
      builder.withProjects(FluentIterable.from(projects)
          .transform(new Function<IndexableField, String>() {
            @Override
            public String apply(IndexableField in) {
              return in.stringValue();
            }
          }).toList());
      return builder.build();
    }
  }

  private static List<IndexableField> fields(Document doc,
      String name) {
    ImmutableList.Builder<IndexableField> builder = ImmutableList.builder();
    for (IndexableField f : doc) {
      if (f.name().equals(name)) {
        builder.add(f);
      }
    }
    return builder.build();
  }
}
