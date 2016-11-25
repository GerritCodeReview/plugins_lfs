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

package com.googlesource.gerrit.plugins.index;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.config.SitePaths;
import com.google.gerrit.server.index.Index;
import com.google.gerrit.server.index.IndexCollection;
import com.google.gerrit.server.index.IndexDefinition;
import com.google.gerrit.server.index.IndexDefinition.IndexFactory;
import com.google.gerrit.server.index.OnlineReindexer;
import com.google.gerrit.server.index.Schema;
import com.google.inject.ProvisionException;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Copy of com.google.gerrit.lucene.LuceneVersionManager however adjusted to
 * plugin context by modification of:
 * - {@link #scanVersions(IndexDefinition, PluginGerritIndexStatus)} - there is no
 * point in walking over existing directory
 * - {@link #initIndex(IndexDefinition, PluginGerritIndexStatus)} - first run of
 * plugin index is detected and re-index started
 * TODO modify access to base class and derive this from there
 */
public abstract class LucenePluginVersionManager implements LifecycleListener {
  private static final Logger log = LoggerFactory
      .getLogger(LucenePluginVersionManager.class);

  private final SitePaths sitePaths;
  private final Map<String, IndexDefinition<?, ?, ?>> defs;
  private final Map<String, OnlineReindexer<?, ?, ?>> reindexers;
  private final boolean onlineUpgrade;

  public static Path getDir(SitePaths sitePaths, String prefix, Schema<?> schema) {
    return sitePaths.index_dir.resolve(String.format("%s%04d",
        prefix, schema.getVersion()));
  }

  protected LucenePluginVersionManager(Config cfg,
      SitePaths sitePaths,
      List<IndexDefinition<?, ?, ?>> defs) {
    this.sitePaths = sitePaths;
    this.defs = Maps.newHashMapWithExpectedSize(defs.size());
    for (IndexDefinition<?, ?, ?> def : defs) {
      this.defs.put(def.getName(), def);
    }
    this.reindexers = Maps.newHashMapWithExpectedSize(defs.size());
    this.onlineUpgrade = cfg.getBoolean("index", null, "onlineUpgrade", true);
  }

  @Override
  public void start() {
    PluginGerritIndexStatus cfg;
    try {
      cfg = new PluginGerritIndexStatus(sitePaths);
    } catch (ConfigInvalidException | IOException e) {
      throw fail(e);
    }

    validateIndexDir();
    for (IndexDefinition<?, ?, ?> def : defs.values()) {
      initIndex(def, cfg);
    }
  }

  @Override
  public void stop() {
  }

  private <K, V, I extends Index<K, V>> void initIndex(
      IndexDefinition<K, V, I> def, PluginGerritIndexStatus cfg) {
    TreeMap<Integer, Version<V>> versions = scanVersions(def, cfg);
    // Search from the most recent ready version.
    // Write to the most recent ready version and the most recent version.
    Version<V> search = null;
    List<Version<V>> write = Lists.newArrayListWithCapacity(2);
    for (Version<V> v : versions.descendingMap().values()) {
      if (v.schema == null) {
        continue;
      }
      if (write.isEmpty() && onlineUpgrade) {
        write.add(v);
      }
      if (v.ready) {
        search = v;
        if (!write.contains(v)) {
          write.add(v);
        }
        break;
      }
    }

    boolean firstRun = false;
    if (search == null) {
      // this is the case when plugin is started for the first time
      // running re-index of gerrit is not a solution, one needs to
      // write to it and start re-index
      Schema<V> latest = def.getLatest();
      search = new Version<>(latest, latest.getVersion(), false, false);
      firstRun = true;
    }

    IndexFactory<K, V, I> factory = def.getIndexFactory();
    I searchIndex = factory.create(search.schema);
    IndexCollection<K, V, I> indexes = def.getIndexCollection();
    indexes.setSearchIndex(searchIndex);

    // there is no need to go ever versions and re-indexers for the first run
    // as it is clear that re-index operation needs to be performed
    if (firstRun) {
      indexes.addWriteIndex(searchIndex);
      OnlineReindexer<K, V, I> reindexer = new OnlineReindexer<>(def, search.version);
      synchronized (this) {
        reindexers.put(def.getName(), reindexer);
        if (onlineUpgrade) {
          reindexer.start();
        }
      }
      return;
    }

    for (Version<V> v : write) {
      if (v.schema != null) {
        if (v.version != search.version) {
          indexes.addWriteIndex(factory.create(v.schema));
        } else {
          indexes.addWriteIndex(searchIndex);
        }
      }
    }

    markNotReady(cfg, def.getName(), versions.values(), write);

    int latest = write.get(0).version;
    OnlineReindexer<K, V, I> reindexer = new OnlineReindexer<>(def, latest);
    synchronized (this) {
      if (!reindexers.containsKey(def.getName())) {
        reindexers.put(def.getName(), reindexer);
        if (onlineUpgrade && latest != search.version) {
          reindexer.start();
        }
      }
    }
  }

  private <V> void markNotReady(PluginGerritIndexStatus cfg, String name,
      Iterable<Version<V>> versions, Collection<Version<V>> inUse) {
    boolean dirty = false;
    for (Version<V> v : versions) {
      if (!inUse.contains(v) && v.exists) {
        cfg.setReady(name, v.version, false);
        dirty = true;
      }
    }
    if (dirty) {
      try {
        cfg.save();
      } catch (IOException e) {
        throw fail(e);
      }
    }
  }

  private <K, V, I extends Index<K, V>> TreeMap<Integer, Version<V>> scanVersions(
      IndexDefinition<K, V, I> def, PluginGerritIndexStatus cfg) {
    TreeMap<Integer, Version<V>> versions = new TreeMap<>();
    for (Schema<V> schema : def.getSchemas().values()) {
      // This part is Lucene-specific.
      Path p = getDir(sitePaths, def.getName(), schema);
      boolean isDir = Files.isDirectory(p);
      if (Files.exists(p) && !isDir) {
        log.warn("Not a directory: %s", p.toAbsolutePath());
      }
      int v = schema.getVersion();
      versions.put(v,
          new Version<>(schema, v, isDir, cfg.getReady(def.getName(), v)));
    }

    return versions;
  }

  private void validateIndexDir() {
    String msg = "No index versions ready; run java -jar " +
        sitePaths.gerrit_war.toAbsolutePath() +
        " reindex";
    if (!Files.exists(sitePaths.index_dir)) {
      throw new ProvisionException(msg);
    } else if (!Files.exists(sitePaths.index_dir)) {
      log.warn("Not a directory: %s", sitePaths.index_dir.toAbsolutePath());
      throw new ProvisionException(msg);
    }
  }

  private ProvisionException fail(Throwable t) {
    ProvisionException e = new ProvisionException("Error scanning indexes");
    e.initCause(t);
    throw e;
  }

  private static class Version<V> {
    private final Schema<V> schema;
    private final int version;
    private final boolean exists;
    private final boolean ready;

    private Version(Schema<V> schema, int version, boolean exists,
        boolean ready) {
      checkArgument(schema == null || schema.getVersion() == version);
      this.schema = schema;
      this.version = version;
      this.exists = exists;
      this.ready = ready;
    }
  }
}
