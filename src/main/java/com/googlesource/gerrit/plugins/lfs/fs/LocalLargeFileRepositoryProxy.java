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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import java.io.IOException;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.Response;
import org.eclipse.jgit.lfs.server.fs.FileLfsRepository;

public class LocalLargeFileRepositoryProxy extends FileLfsRepository {
  public interface Factory {
    LocalLargeFileRepositoryProxy create(LfsBackend backend);
  }

  private final LocalLargeFileRepository.Factory repoLfs;
  private final LfsBackend backend;
  private final String servletUrlPattern;

  @Inject
  LocalLargeFileRepositoryProxy(
      LfsFsDataDirectoryManager dataDirManager,
      LocalLargeFileRepository.Factory repoLfs,
      @Assisted LfsBackend backend)
      throws IOException {
    super(null, dataDirManager.ensureForRepoBackend(backend));
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
}
