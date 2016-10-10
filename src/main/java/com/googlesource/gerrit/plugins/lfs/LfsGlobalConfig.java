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

package com.googlesource.gerrit.plugins.lfs;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import org.eclipse.jgit.lib.Config;

import java.util.Map;

/**
 * Represents the global LFS configuration stored in $SITE/etc/lfs.config.
 */
public class LfsGlobalConfig {

  private final Config cfg;

  LfsGlobalConfig(Config cfg) {
    this.cfg = cfg;
  }

  public LfsBackend getDefaultBackend() {
    LfsBackendType type =
        cfg.getEnum("storage", null, "backend", LfsBackendType.FS);
    return new LfsBackend(null, type);
  }

  public Map<String, LfsBackend> getBackends() {
    Builder<String, LfsBackend> builder = ImmutableMap.builder();
    for (final LfsBackendType type : LfsBackendType.values()) {
      Map<String, LfsBackend> backendsOfType =
          FluentIterable.from(cfg.getSubsections(type.name()))
              .toMap(new Function<String, LfsBackend>() {
                @Override
                public LfsBackend apply(String input) {
                  return new LfsBackend(input, type);
                }
              });
      builder.putAll(backendsOfType);
    }

    return builder.build();
  }

  public String getString(String section, String subsection, String name) {
    return cfg.getString(section, subsection, name);
  }

  public int getInt(String section, String subsection, String name,
      int defaultValue) {
    return cfg.getInt(section, subsection, name, defaultValue);
  }

  public boolean getBoolean(String section, String subsection, String name,
      boolean defaultValue) {
    return cfg.getBoolean(section, subsection, name, defaultValue);
  }
}
