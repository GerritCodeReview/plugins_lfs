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

import com.google.common.base.Strings;
import java.util.Objects;

public class LfsBackend {
  public static final String DEFAULT = "default";

  public final String name;
  public final LfsBackendType type;

  public static LfsBackend create(String name, LfsBackendType type) {
    return new LfsBackend(name, type);
  }

  public static LfsBackend createDefault(LfsBackendType type) {
    return new LfsBackend(null, type);
  }

  private LfsBackend(String name, LfsBackendType type) {
    this.name = Strings.isNullOrEmpty(name) ? DEFAULT : name;
    this.type = type;
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
}
