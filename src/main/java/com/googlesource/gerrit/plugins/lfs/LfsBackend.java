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

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Objects;

public class LfsBackend {
  public static final String DEFAULT = "default";
  public static final LfsBackendVersion DEFAULT_VERSION = LfsBackendVersion.V1;

  public final String name;
  public final LfsBackendType type;
  public final LfsBackendVersion version;

  public LfsBackend(String name, LfsBackendType type, LfsBackendVersion version) {
    this.name = name;
    this.type = type;
    this.version = version;
  }

  @Override
  public int hashCode() {
    return Objects.hash(isNullOrEmpty(name) ? DEFAULT : name, type);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LfsBackend) {
      LfsBackend other = (LfsBackend) obj;
      return Objects.equals(name, other.name)
          && type == other.type
          && Objects.equals(version, other.version);
    }

    return false;
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .add("name", isNullOrEmpty(name) ? DEFAULT : name)
        .add("type", type)
        .add("version", version)
        .toString();
  }
}
