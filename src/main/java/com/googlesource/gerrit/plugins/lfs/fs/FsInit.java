// Copyright (C) 2018 The Android Open Source Project
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

import static com.googlesource.gerrit.plugins.lfs.LfsBackendType.FS;
import static com.googlesource.gerrit.plugins.lfs.LfsBackendVersion.V2;
import static com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig.STORAGE;
import static com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig.VERSION;
import static com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository.DIRECTORY;
import static com.googlesource.gerrit.plugins.lfs.locks.LfsLocksPathProvider.LFS_LOCKS;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FsInit {
  private static final Logger log = LoggerFactory.getLogger(FsInit.class);

  private final Path cfgPath;
  private final Path defaultDataDir;

  @Inject
  FsInit(SitePaths site, @PluginName String name) {
    this.cfgPath = site.etc_dir.resolve(name + ".config");
    ;
    this.defaultDataDir = site.data_dir.resolve(name);
  }

  /**
   * Only FS storage based backends are automatically migrated and only provided that they don't
   * contain any LFS data yet
   */
  public void init() throws Exception {
    FileBasedConfig cfg = getPluginConfig();
    LfsGlobalConfig lfsCfg = new LfsGlobalConfig(cfg);
    List<LfsBackend> candidates =
        Stream.concat(Stream.of(lfsCfg.getDefaultBackend()), lfsCfg.getBackends().values().stream())
            .filter(b -> shouldMigrate(b))
            .collect(toList());
    if (!candidates.isEmpty()) {
      candidates.forEach(
          b -> {
            Path dir = getStorageDir(lfsCfg, b);
            if (Files.isDirectory(dir) && containsLfsData(dir)) {
              log.debug("Backend [{}] contains already LFS data and cannot be migrated");
            }

            cfg.setEnum(STORAGE, b.name, VERSION, V2);
          });
      cfg.save();
    }
  }

  private Path getStorageDir(LfsGlobalConfig lfsCfg, LfsBackend backend) {
    String dataDir = lfsCfg.getString(backend.type.name(), backend.name, DIRECTORY);
    if (!Strings.isNullOrEmpty(dataDir)) {
      return Paths.get(dataDir);
    }

    return defaultDataDir;
  }

  private static boolean containsLfsData(Path storage) {
    Path lfsLocks = Paths.get(LFS_LOCKS);
    try (Stream<Path> content = Files.list(storage)) {
      return content.findAny().isPresent() && content.anyMatch(p -> !p.endsWith(lfsLocks));
    } catch (IOException e) {
      log.warn("Checking storage [{}] content failed with the following error", storage, e);
      return true;
    }
  }

  private FileBasedConfig getPluginConfig() throws IOException, ConfigInvalidException {
    FileBasedConfig cfg = new FileBasedConfig(cfgPath.toFile(), org.eclipse.jgit.util.FS.DETECTED);
    cfg.load();
    return cfg;
  }

  private static boolean shouldMigrate(LfsBackend backend) {
    return FS == backend.type && V2 != backend.version;
  }
}
