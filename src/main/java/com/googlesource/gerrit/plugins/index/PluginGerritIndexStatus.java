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

import com.google.gerrit.server.config.SitePaths;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;

import java.io.IOException;

// TODO copy of com.google.gerrit.lucene.GerritIndexStatus
// remove this class once access is changed
class PluginGerritIndexStatus {
  private static final String SECTION = "index";
  private static final String KEY_READY = "ready";

  private final FileBasedConfig cfg;

  PluginGerritIndexStatus(SitePaths sitePaths)
      throws ConfigInvalidException, IOException {
    cfg = new FileBasedConfig(
        sitePaths.index_dir.resolve("gerrit_index.config").toFile(),
        FS.detect());
    cfg.load();
  }

  void setReady(String indexName, int version, boolean ready) {
    cfg.setBoolean(SECTION, indexDirName(indexName, version), KEY_READY, ready);
  }

  boolean getReady(String indexName, int version) {
    return cfg.getBoolean(SECTION, indexDirName(indexName, version), KEY_READY,
        false);
  }

  void save() throws IOException {
    cfg.save();
  }

  private static String indexDirName(String indexName, int version) {
    return String.format("%s_%04d", indexName, version);
  }
}
