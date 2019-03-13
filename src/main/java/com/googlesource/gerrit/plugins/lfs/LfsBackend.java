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

import com.google.common.base.Strings;
import com.google.gerrit.common.Nullable;
import java.util.Objects;

public class LfsBackend {
  private static final String DEFAULT = "default";

  public final String name;
  public final LfsBackendType type;

  public static LfsBackend create(@Nullable String name, LfsBackendType type) {
    return new LfsBackend(name, type);
  }

  public static LfsBackend createDefault(LfsBackendType type) {
    return create(null, type);
  }

  private LfsBackend(String name, LfsBackendType type) {
    this.name = name;
    this.type = type;
  }

  /** @return the backend name, or the default name if null. */
  public String name() {
    return Strings.isNullOrEmpty(name) ? DEFAULT : name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LfsBackend) {
      LfsBackend other = (LfsBackend) obj;
      return Objects.equals(name, other.name) && type == other.type;
    }

    return false;
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("name", name()).add("type", type).toString();
  }
}
