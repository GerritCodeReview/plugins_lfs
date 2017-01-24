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

import static com.googlesource.gerrit.plugins.lfs.LfsBackend.DEFAULT;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginCanonicalWebUrl;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.googlesource.gerrit.plugins.lfs.AuthInfo;
import com.googlesource.gerrit.plugins.lfs.ExpiringAction;
import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.Response;
import org.eclipse.jgit.lfs.server.fs.FileLfsRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalLargeFileRepository extends FileLfsRepository {
  public interface Factory {
    LocalLargeFileRepository create(LfsBackend backendConfig);
  }

  public static final String CONTENT_PATH = "content";
  public static final String UPLOAD = "upload";
  public static final String DOWNLOAD = "download";
  private static final int DEFAULT_TIMEOUT = 10; //in seconds

  private final String servletUrlPattern;
  private final LfsFsRequestAuthorizer authorizer;
  private final int expirationSeconds;

  @Inject
  LocalLargeFileRepository(LfsConfigurationFactory configFactory,
      LfsFsRequestAuthorizer authorizer,
      @PluginCanonicalWebUrl String url,
      @PluginData Path defaultDataDir,
      @Assisted LfsBackend backend) throws IOException {
    super(getContentUrl(url, backend),
        getOrCreateDataDir(configFactory.getGlobalConfig(),
            backend, defaultDataDir));
    this.authorizer = authorizer;
    this.servletUrlPattern = "/" + getContentPath(backend) + "*";
    this.expirationSeconds = configFactory.getGlobalConfig()
        .getInt(backend.type.name(), backend.name, "expirationSeconds",
            DEFAULT_TIMEOUT);
  }

  public String getServletUrlPattern() {
    return servletUrlPattern;
  }

  @Override
  public Response.Action getDownloadAction(AnyLongObjectId id) {
    Response.Action action = super.getDownloadAction(id);
    AuthInfo authInfo =
        authorizer.generateAuthInfo(DOWNLOAD, id, expirationSeconds);
    return new ExpiringAction(action.href, authInfo);
  }

  @Override
  public Response.Action getUploadAction(AnyLongObjectId id, long size) {
    Response.Action action = super.getUploadAction(id, size);
    AuthInfo authInfo =
        authorizer.generateAuthInfo(UPLOAD, id, expirationSeconds);
    return new ExpiringAction(action.href, authInfo);
  }

  private static String getContentUrl(String url, LfsBackend backend) {
    // for default FS we still need to define namespace as otherwise it would
    // interfere with rest of FS backends
    return url + (url.endsWith("/") ? "" : "/") + getContentPath(backend);
  }

  private static String getContentPath(LfsBackend backend) {
    return CONTENT_PATH + "/"
        + (Strings.isNullOrEmpty(backend.name) ? DEFAULT : backend.name) + "/";
  }

  private static Path getOrCreateDataDir(LfsGlobalConfig config,
      LfsBackend backendConfig, Path defaultDataDir)
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
