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

import static com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository.DEFAULT_TIMEOUT;
import static com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository.getContentPath;
import static com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository.getOrCreateDataDir;
import static com.googlesource.gerrit.plugins.lfs.fs.LocalProjectBackendLargeFileRepository.STORE_IN_REPO_DIR;
import static com.googlesource.gerrit.plugins.lfs.fs.LocalProjectBackendLargeFileRepository.resolvePath;
import static org.eclipse.jgit.lfs.lib.Constants.DOWNLOAD;
import static org.eclipse.jgit.lfs.lib.Constants.UPLOAD;
import static org.eclipse.jgit.lib.Constants.DOT_GIT_EXT;

import com.google.gerrit.extensions.annotations.PluginCanonicalWebUrl;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.lfs.AuthInfo;
import com.googlesource.gerrit.plugins.lfs.ExpiringAction;
import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.Response;
import org.eclipse.jgit.lib.Config;

public class LocalProjectLargeFileRepository extends NamedFileLfsRepository {
  public interface Factory {
    LocalProjectLargeFileRepository create(LfsBackend backendConfig, String repo);
  }

  private final LfsFsRequestAuthorizer authorizer;
  private final Long expiresIn;

  @Inject
  LocalProjectLargeFileRepository(
      LfsConfigurationFactory configFactory,
      LfsFsRequestAuthorizer authorizer,
      @PluginCanonicalWebUrl String url,
      @PluginData Path defaultDataDir,
      @GerritServerConfig Config config,
      @Assisted LfsBackend backend,
      @Assisted String repo)
      throws IOException {
    super(
        getRepoContentUrl(url, repo, backend),
        getOrCreateRepoDataDir(
            config, configFactory.getGlobalConfig(), backend, defaultDataDir, repo),
        repo);
    this.authorizer = authorizer;
    this.expiresIn =
        (long)
            configFactory
                .getGlobalConfig()
                .getInt(backend.type.name(), backend.name, "expirationSeconds", DEFAULT_TIMEOUT);
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

  private static String getRepoContentUrl(String url, String repo, LfsBackend backend) {
    return url + (url.endsWith("/") ? "" : "/") + getContentPath(backend) + repo + "/";
  }

  private static Path getOrCreateRepoDataDir(
      Config config, LfsGlobalConfig cfg, LfsBackend backend, Path defaultDataDir, String repo)
      throws IOException {
    Path backendPath = getOrCreateDataDir(cfg, backend, defaultDataDir);
    String repoName =
        cfg.getBoolean(backend.type.name(), backend.name, STORE_IN_REPO_DIR, false)
            ? String.format("%s%s/%%binaries%%", repo, DOT_GIT_EXT)
            : repo;
    Path ensured =
        Files.createDirectories(resolvePath(config, cfg, backend, backendPath).resolve(repoName));

    if (!Files.isReadable(ensured)) {
      throw new IOException("Path '" + ensured.toAbsolutePath() + "' cannot be accessed");
    }

    return ensured;
  }
}
