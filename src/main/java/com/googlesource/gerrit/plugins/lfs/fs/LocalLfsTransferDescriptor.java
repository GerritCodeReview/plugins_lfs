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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.LfsBackendConfig;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.fs.FileLfsRepository;
import org.eclipse.jgit.lfs.server.fs.FileLfsTransferDescriptor;

import java.util.Map;

@Singleton
public class LocalLfsTransferDescriptor extends FileLfsTransferDescriptor {
  private final LocalLargeFileRepository.Factory fsRepoFactory;
  private final LfsBackendConfig defaultBackend;
  private final Map<String, LfsBackendConfig> backends;

  @Inject
  LocalLfsTransferDescriptor(LocalLargeFileRepository.Factory fsRepoFactory,
      LfsConfigurationFactory configFactory) {
    this.fsRepoFactory = fsRepoFactory;

    LfsGlobalConfig config = configFactory.getGlobalConfig();
    this.defaultBackend = config.getDefaultBackend();
    this.backends = config.getBackends();
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
    LfsBackendConfig config = defaultBackend;
    if (!Strings.isNullOrEmpty(backend)) {
      config = backends.get(backend);
      if (config == null) {
        throw new IllegalArgumentException(
            "There is no FileLfsRepository [" + backend + "] configured");
      }
    }

    return fsRepoFactory.create(config);
  }
}
