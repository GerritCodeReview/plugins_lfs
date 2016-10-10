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

import static com.googlesource.gerrit.plugins.lfs.LfsBackendConfig.DEFAULT;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginCanonicalWebUrl;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.googlesource.gerrit.plugins.lfs.LfsBackendConfig;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig;

import org.eclipse.jgit.lfs.server.fs.FileLfsRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalLargeFileRepository extends FileLfsRepository {
  public interface Factory {
    LocalLargeFileRepository create(LfsBackendConfig backendConfig);
  }

  public static final String CONTENT_PATH = "content";

  private final String servletRegexp;

  @Inject
  LocalLargeFileRepository(LfsConfigurationFactory configFactory,
      @PluginCanonicalWebUrl String url,
      @PluginData Path defaultDataDir,
      @Assisted LfsBackendConfig backend) throws IOException {
    super(getContentUrl(url, backend),
        getOrCreateDataDir(configFactory.getGlobalConfig(),
            backend, defaultDataDir));
    this.servletRegexp = "/" + getContentPath(backend) + "*";
  }

  public String getServletRegexp() {
    return servletRegexp;
  }

  private static String getContentUrl(String url, LfsBackendConfig backend) {
    //for default FS we still need to define namespace as othewise it would
    //interfere with rest of FS backends
    return url + (url.endsWith("/") ? "" : "/") + getContentPath(backend);
  }

  private static String getContentPath(LfsBackendConfig backend) {
    return CONTENT_PATH + "/"
        + (Strings.isNullOrEmpty(backend.name) ? DEFAULT : backend.name) + "/";
  }

  private static Path getOrCreateDataDir(LfsGlobalConfig config,
      LfsBackendConfig backendConfig, Path defaultDataDir)
      throws IOException {
    String dataDir = config.getString(
        backendConfig.type.name(), backendConfig.name, "directory");
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
