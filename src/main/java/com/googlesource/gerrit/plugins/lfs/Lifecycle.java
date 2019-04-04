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

package com.googlesource.gerrit.plugins.lfs;

import com.google.common.base.Strings;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.jgit.lib.Config;

@Singleton
public class Lifecycle implements LifecycleListener {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private final String name;
  private final Config config;

  @Inject
  Lifecycle(@PluginName String name, @GerritServerConfig Config config) {
    this.name = name;
    this.config = config;
  }

  @Override
  public void start() {
    String plugin = config.getString("lfs", null, "plugin");
    if (Strings.isNullOrEmpty(plugin)) {
      warn("lfs.plugin is not set");
    } else if (!plugin.equals(name)) {
      warn(String.format("lfs.plugin is set, but is not set to '%s'", name));
    }
  }

  private void warn(String msg) {
    log.atWarning().log(
        "%s; LFS will not be enabled. Run site initialization, or manually set"
            + " lfs.plugin to '%s' in gerrit.config",
        msg, name);
  }

  @Override
  public void stop() {}
}
