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

import static org.eclipse.jgit.lib.Constants.DOT_GIT_EXT;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.eclipse.jgit.lib.Config;

@Singleton
public class LfsFsDataDirectoryManager {
  static final String STORE_IN_REPO_DIR = "storeDataInRepoDotGit";
  private static final String KEY_DIRECTORY = "directory";
  private static final String ROOT_DIR = "repositories";

  private final LfsConfigurationFactory configFactory;
  private final SitePaths site;
  private final Path defaultDataDir;
  private final Config config;

  @Inject
  LfsFsDataDirectoryManager(
      LfsConfigurationFactory configFactory,
      SitePaths site,
      @PluginData Path defaultDataDir,
      @GerritServerConfig Config config) {
    this.configFactory = configFactory;
    this.site = site;
    this.defaultDataDir = defaultDataDir;
    this.config = config;
  }

  public Path ensureForBackend(LfsBackend backend) throws IOException {
    return getForBackend(backend, true);
  }

  public Path ensureForRepoBackend(LfsBackend backend, String repo) throws IOException {
    boolean storeInRepo =
        configFactory
            .getGlobalConfig()
            .getBoolean(backend.type.name(), backend.name, STORE_IN_REPO_DIR, false);
    if (storeInRepo) {
      Path path =
          Optional.ofNullable(config.getString("gerrit", null, "basePath"))
              .map(p -> site.resolve(p))
              .orElse(getForBackend(backend, false));
      return ensureDirsForBackend(path, repo + DOT_GIT_EXT, "%binaries%");
    }

    return ensureDirsForBackend(getForBackend(backend, false), ROOT_DIR, repo);
  }

  public Path ensureForRepoBackend(LfsBackend backend) throws IOException {
    boolean storeInRepo =
        configFactory
            .getGlobalConfig()
            .getBoolean(backend.type.name(), backend.name, STORE_IN_REPO_DIR, false);
    if (storeInRepo) {
      Path path =
          Optional.ofNullable(config.getString("gerrit", null, "basePath"))
              .map(p -> site.resolve(p))
              .orElse(getForBackend(backend, false));
      return ensureDirsForBackend(path);
    }

    return ensureDirsForBackend(getForBackend(backend, false), ROOT_DIR);
  }

  public Path getForBackend(LfsBackend backend, boolean ensure) throws IOException {
    String dataDir =
        configFactory.getGlobalConfig().getString(backend.type.name(), backend.name, KEY_DIRECTORY);
    if (Strings.isNullOrEmpty(dataDir)) {
      return defaultDataDir;
    }

    if (ensure) {
      return ensureDir(dataDir);
    }
    return Paths.get(dataDir);
  }

  private Path ensureDirsForBackend(Path path, String... dirs) throws IOException {
    for (String dir : dirs) {
      path = path.resolve(dir);
    }
    return ensureDir(path.toString());
  }

  private Path ensureDir(String dataDir) throws IOException {
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
}
