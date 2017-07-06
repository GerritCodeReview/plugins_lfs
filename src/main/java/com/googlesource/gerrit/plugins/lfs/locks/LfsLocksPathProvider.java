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

import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginData;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
class LfsLocksPathProvider implements Provider<String> {
  private final String path;

  @Inject
  LfsLocksPathProvider(LfsConfigurationFactory configFactory,
      @PluginData Path defaultDataDir) {
    String dataDir = configFactory.getGlobalConfig().getString("locks", null, "directory");
    this.path = Strings.isNullOrEmpty(dataDir)
            ? Paths.get(defaultDataDir.toString(), "lfs_locks").toString()
            : dataDir;
  }

  @Override
  public String get() {
    return path;
  }
}
