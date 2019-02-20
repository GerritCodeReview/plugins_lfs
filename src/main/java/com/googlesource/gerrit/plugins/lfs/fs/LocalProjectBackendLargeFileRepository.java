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

import static com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository.getContentPath;
import static com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository.getOrCreateDataDir;

import com.google.gerrit.extensions.annotations.PluginCanonicalWebUrl;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.Response;
import org.eclipse.jgit.lfs.server.fs.FileLfsRepository;
import org.eclipse.jgit.lib.Config;

public class LocalProjectBackendLargeFileRepository extends FileLfsRepository {
  public interface Factory {
    LocalProjectBackendLargeFileRepository create(LfsBackend backend);
  }

  static final String STORE_IN_REPO_DIR = "storeDataInRepoDotGit";
  private static final String ROOT_DIR = "repository";

  private final LocalProjectLargeFileRepository.Factory repoLfs;
  private final LfsBackend backend;
  private final String servletUrlPattern;

  @Inject
  LocalProjectBackendLargeFileRepository(
      LocalProjectLargeFileRepository.Factory repoLfs,
      LfsConfigurationFactory configFactory,
      @PluginCanonicalWebUrl String url,
      @PluginData Path defaultDataDir,
      @GerritServerConfig Config config,
      @Assisted LfsBackend backend)
      throws IOException {
    super(
        null,
        getOrCreateRootDataDir(config, configFactory.getGlobalConfig(), backend, defaultDataDir));
    this.repoLfs = repoLfs;
    this.backend = backend;
    this.servletUrlPattern = "/" + getContentPath(backend) + "*";
  }

  public String getServletUrlPattern() {
    return servletUrlPattern;
  }

  public FileLfsRepository getRepository(String repo) {
    return repoLfs.create(backend, repo);
  }

  @Override
  public Response.Action getDownloadAction(AnyLongObjectId id) {
    throw new UnsupportedOperationException(
        "This is LFS FS repository proxy shouldn't be used to get data");
  }

  @Override
  public Response.Action getUploadAction(AnyLongObjectId id, long size) {
    throw new UnsupportedOperationException(
        "This is LFS FS repository proxy shouldn't be used to upload data");
  }

  @Override
  public Response.Action getVerifyAction(AnyLongObjectId id) {
    throw new UnsupportedOperationException(
        "This is LFS FS repository proxy shouldn't be used to verify data");
  }

  @Override
  public long getSize(AnyLongObjectId id) throws IOException {
    throw new UnsupportedOperationException(
        "This is LFS FS repository proxy shouldn't be used to get data size");
  }

  static Path resolvePath(
      Config config, LfsGlobalConfig cfg, LfsBackend backend, Path backendPath) {
    boolean storeInRepo =
        cfg.getBoolean(backend.type.name(), backend.name, STORE_IN_REPO_DIR, false);
    if (storeInRepo) {
      return Paths.get(config.getString("gerrit", null, "basePath"));
    }
    return backendPath.resolve(ROOT_DIR);
  }

  private static Path getOrCreateRootDataDir(
      Config config, LfsGlobalConfig cfg, LfsBackend backend, Path defaultDataDir)
      throws IOException {
    Path backendPath = getOrCreateDataDir(cfg, backend, defaultDataDir);
    Path ensured = Files.createDirectories(resolvePath(config, cfg, backend, backendPath));

    if (!Files.isReadable(ensured)) {
      throw new IOException("Path '" + ensured.toAbsolutePath() + "' cannot be accessed");
    }

    return ensured;
  }
}
