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

import org.eclipse.jgit.lib.Config;

public class LfsProjectConfigSection {
  public static final String LFS = "lfs";
  public static final String KEY_MAX_OBJECT_SIZE = "maxObjectSize";
  public static final String KEY_ENABLED = "enabled";
  public static final String KEY_READ_ONLY = "readOnly";
  public static final String KEY_BACKEND = "backend";

  private final Config cfg;
  private final String namespace;

  LfsProjectConfigSection(Config cfg, String namespace) {
    this.cfg = cfg;
    this.namespace = namespace;
  }

  public String getNamespace() {
    return namespace;
  }

  public long getMaxObjectSize() {
    return cfg.getLong(LFS, namespace, KEY_MAX_OBJECT_SIZE, 0);
  }

  public boolean isEnabled() {
    return cfg.getBoolean(LFS, namespace, KEY_ENABLED, false);
  }

  public boolean isReadOnly() {
    return cfg.getBoolean(LFS, namespace, KEY_READ_ONLY, false);
  }

  public String getBackend() {
    return cfg.getString(LFS, namespace, KEY_BACKEND);
  }
}
