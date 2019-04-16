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

import static org.eclipse.jgit.lfs.lib.Constants.DOWNLOAD;
import static org.eclipse.jgit.lfs.lib.Constants.UPLOAD;

import com.google.gerrit.common.Nullable;
import com.google.gerrit.extensions.annotations.PluginCanonicalWebUrl;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.auth.AuthInfo;
import com.googlesource.gerrit.plugins.lfs.auth.ExpiringAction;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.Response;
import org.eclipse.jgit.lfs.server.fs.FileLfsRepository;

public class LocalLargeFileRepository extends FileLfsRepository {
  public interface Factory {
    LocalLargeFileRepository create(LfsBackend backendConfig);

    LocalLargeFileRepository create(LfsBackend backendConfig, String repo);
  }

  static final String DIRECTORY = "directory";
  static final int DEFAULT_EXPIRATION_SECONDS = 10;
  private static final String CONTENT_PATH_TEMPLATE = "content/%s/";

  private final String servletUrlPattern;
  private final LfsFsRequestAuthorizer authorizer;
  private final Long expiresIn;
  private final String repo;

  @AssistedInject
  LocalLargeFileRepository(
      LfsFsDataDirectoryManager dataDirManager,
      LfsConfigurationFactory configFactory,
      LfsFsRequestAuthorizer authorizer,
      @PluginCanonicalWebUrl String url,
      @Assisted LfsBackend backend)
      throws IOException {
    this(
        getContentUrl(url, backend),
        dataDirManager.ensureForBackend(backend),
        configFactory,
        authorizer,
        backend,
        "/" + getContentPath(backend) + "*",
        null);
  }

  @AssistedInject
  LocalLargeFileRepository(
      LfsFsDataDirectoryManager dataDirManager,
      LfsConfigurationFactory configFactory,
      LfsFsRequestAuthorizer authorizer,
      @PluginCanonicalWebUrl String url,
      @Assisted LfsBackend backend,
      @Assisted String repo)
      throws IOException {
    this(
        getRepoContentUrl(url, repo, backend),
        dataDirManager.ensureForRepoBackend(backend, repo),
        configFactory,
        authorizer,
        backend,
        null,
        repo);
  }

  private LocalLargeFileRepository(
      String url,
      Path dataDir,
      LfsConfigurationFactory configFactory,
      LfsFsRequestAuthorizer authorizer,
      LfsBackend backend,
      @Nullable String servletUrlPattern,
      @Nullable String repo)
      throws IOException {
    super(url, dataDir);
    this.authorizer = authorizer;
    this.servletUrlPattern = servletUrlPattern;
    this.expiresIn =
        (long)
            configFactory
                .getGlobalConfig()
                .getInt(
                    backend.type.name(),
                    backend.name,
                    "expirationSeconds",
                    DEFAULT_EXPIRATION_SECONDS);
    this.repo = repo;
  }

  public String getServletUrlPattern() {
    return servletUrlPattern;
  }

  public String getRepo() {
    return repo;
  }

  @Override
  public Response.Action getDownloadAction(AnyLongObjectId id) {
    Response.Action action = super.getDownloadAction(id);
    AuthInfo authInfo = authorizer.generateAuthInfo(DOWNLOAD, id, Instant.now(), expiresIn);
    return new ExpiringAction(action.href, authInfo);
  }

  @Override
  public Response.Action getUploadAction(AnyLongObjectId id, long size) {
    Response.Action action = super.getUploadAction(id, size);
    AuthInfo authInfo = authorizer.generateAuthInfo(UPLOAD, id, Instant.now(), expiresIn);
    return new ExpiringAction(action.href, authInfo);
  }

  private static String getContentUrl(String url, LfsBackend backend) {
    // for default FS we still need to define namespace as otherwise it would
    // interfere with rest of FS backends
    return url + (url.endsWith("/") ? "" : "/") + getContentPath(backend);
  }

  private static String getRepoContentUrl(String url, String repo, LfsBackend backend) {
    return url + (url.endsWith("/") ? "" : "/") + getContentPath(backend) + repo + "/";
  }

  static String getContentPath(LfsBackend backend) {
    return String.format(CONTENT_PATH_TEMPLATE, backend.name());
  }
}
