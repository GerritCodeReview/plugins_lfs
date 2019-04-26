// Copyright (C) 2019 The Android Open Source Project
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

import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class LfsFsDataDirectoryManager {
  private static final String KEY_DIRECTORY = "directory";

  private final LfsConfigurationFactory configFactory;
  private final Path defaultDataDir;

  @Inject
  LfsFsDataDirectoryManager(
      LfsConfigurationFactory configFactory, @PluginData Path defaultDataDir) {
    this.configFactory = configFactory;
    this.defaultDataDir = defaultDataDir;
  }

  public Path ensureForBackend(LfsBackend backend) throws IOException {
    return getForBackend(backend, true);
  }

  public Path getForBackend(LfsBackend backend, boolean ensure) throws IOException {
    String dataDir =
        configFactory.getGlobalConfig().getString(backend.type.name(), backend.name, KEY_DIRECTORY);
    if (Strings.isNullOrEmpty(dataDir)) {
      return defaultDataDir;
    }

    if (ensure) {
      // note that the following method not only creates missing
      // directory/directories but throws exception when path
      // exists and points to file
      Path ensured = Files.createDirectories(Paths.get(dataDir));

      // we should at least make sure that directory is readable
      if (!Files.isReadable(ensured)) {
        throw new IOException("Path '" + ensured.toAbsolutePath() + "' cannot be accessed");
      }

      return ensured;
    }
    return Paths.get(dataDir);
  }
}
