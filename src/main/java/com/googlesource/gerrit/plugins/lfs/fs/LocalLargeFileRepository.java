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

import static com.google.gerrit.extensions.restapi.Url.encode;
import static com.googlesource.gerrit.plugins.lfs.LfsBackend.DEFAULT;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gerrit.extensions.annotations.PluginCanonicalWebUrl;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.gerrit.reviewdb.client.Project;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.LargeFileRepository;
import org.eclipse.jgit.lfs.server.Response.Action;
import org.eclipse.jgit.lfs.server.fs.LinkingFileLfsRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class LocalLargeFileRepository implements LargeFileRepository {
  public interface Factory {
    LocalLargeFileRepository create(LfsBackend backendConfig);
  }

  public static final String CONTENT_PATH = "content";

  private final String url;
  private final Path dir;
  private final String servletUrlPattern;
  private final LoadingCache<String, LinkingFileLfsRepository> repositories;

  @Inject
  LocalLargeFileRepository(LfsConfigurationFactory configFactory,
      @PluginCanonicalWebUrl String url,
      @PluginData Path defaultDataDir,
      @Assisted LfsBackend backend) throws IOException {
    this.url = getContentUrl(url, backend);
    this.dir = getOrCreateDataDir(configFactory.getGlobalConfig(),
        backend, defaultDataDir);
    this.servletUrlPattern = "/" + getContentPath(backend) + "*";
    this.repositories = CacheBuilder
        .newBuilder()
        .expireAfterAccess(15L, TimeUnit.MINUTES)
        .build(new CacheLoader<String, LinkingFileLfsRepository>() {
          @Override
          public LinkingFileLfsRepository load(String project)
              throws Exception {
            return new LinkingFileLfsRepository(
                LocalLargeFileRepository.this.url + encode(project) + "/",
                dir, project);
          }
        });
  }

  public String getServletUrlPattern() {
    return servletUrlPattern;
  }

  public LinkingFileLfsRepository getProjectRepository(Project.NameKey project) {
    return getProjectRepository(project.get());
  }

  public LinkingFileLfsRepository getProjectRepository(String project) {
    return repositories.getUnchecked(project);
  }

  @Override
  public Action getDownloadAction(AnyLongObjectId id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Action getUploadAction(AnyLongObjectId id, long size) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Action getVerifyAction(AnyLongObjectId id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getSize(AnyLongObjectId id) throws IOException {
    throw new UnsupportedOperationException();
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
