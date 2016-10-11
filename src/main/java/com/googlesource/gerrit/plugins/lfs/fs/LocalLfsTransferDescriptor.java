// Copyright (C) 2016 The Android Open Source Project
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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.fs.FileLfsRepository;
import org.eclipse.jgit.lfs.server.fs.FileLfsTransferDescriptor;

@Singleton
public class LocalLfsTransferDescriptor extends FileLfsTransferDescriptor {
  private final LocalFsRepositoriesCache cache;

  @Inject
  LocalLfsTransferDescriptor(LocalFsRepositoriesCache cache) {
    this.cache = cache;
  }

  @Override
  protected AnyLongObjectId getObjectFromPath(String path)
      throws IllegalArgumentException {
    int specDivider = path.lastIndexOf('/');
    return super.getObjectFromPath(path.substring(specDivider));
  }

  @Override
  protected FileLfsRepository getRepositoryFromPath(String path)
      throws IllegalArgumentException {
    int specDivider = path.lastIndexOf('/');
    String backend = path.substring(0, specDivider);
    LocalLargeFileRepository repository = cache.getRepository(backend);
    if (repository == null) {
      throw new IllegalArgumentException(
          "There is no FileLfsRepository [" + backend + "] configured");
    }

    return repository;
  }
}
