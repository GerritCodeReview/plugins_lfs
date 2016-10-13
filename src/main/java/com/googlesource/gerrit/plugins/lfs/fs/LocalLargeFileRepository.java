// Copyright (C) 2015 The Android Open Source Project
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
import com.google.gerrit.extensions.annotations.PluginCanonicalWebUrl;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.LfsBackendType;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig;

import org.eclipse.jgit.lfs.server.fs.FileLfsRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalLargeFileRepository extends FileLfsRepository {
  public static final String CONTENT_PATH = "content";

  @Inject
  LocalLargeFileRepository(LfsConfigurationFactory configFactory,
      @PluginCanonicalWebUrl String url,
      @PluginData Path defaultDataDir) throws IOException {
    super(getContentPath(url),
        getOrCreateDataDir(configFactory.getGlobalConfig(), defaultDataDir));
  }

  private static String getContentPath(String url) {
    return url + (url.endsWith("/") ? CONTENT_PATH : "/" + CONTENT_PATH) + "/";
  }

  private static Path getOrCreateDataDir(LfsGlobalConfig config, Path defaultDataDir)
      throws IOException {
    String dataDir = config.getString(
        LfsBackendType.FS.name(), null, "directory");
    if (Strings.isNullOrEmpty(dataDir)) {
      return defaultDataDir;
    }

    // note that the following method not only creates missing
    // directory/directories but throws exception when path
    // exists and points to file
    Path ensured = Files.createDirectories(Paths.get(dataDir));

    // we should at least make sure that directory is readable
    if (!Files.isReadable(ensured)) {
      throw new IOException(
          "Path '" + ensured.toAbsolutePath() + "' cannot be accessed");
    }

    return ensured;
  }
}
