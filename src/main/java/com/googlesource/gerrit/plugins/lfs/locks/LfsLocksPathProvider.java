// Copyright (C) 2017 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.lfs.locks;

import com.google.common.base.MoreObjects;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class LfsLocksPathProvider implements Provider<String> {
  public static final String LFS_LOCKS = "lfs_locks";

  private final String path;

  @Inject
  LfsLocksPathProvider(LfsConfigurationFactory configFactory, @PluginData Path defaultDataDir) {
    String locksDir = configFactory.getGlobalConfig().getString("locks", null, "directory");
    this.path =
        MoreObjects.firstNonNull(
            locksDir, Paths.get(defaultDataDir.toString(), LFS_LOCKS).toString());
  }

  @Override
  public String get() {
    return path;
  }
}
