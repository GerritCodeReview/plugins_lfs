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

public final class LfsPaths {
  public static final String LFS_OBJECTS_PATH = "objects/batch";
  public static final String LFS_LOCKS_PATH = "locks(?:/(.*)(?:/unlock))?";
  public static final String LFS_VERIFICATION_PATH = "locks/verify";
  public static final String LFS_UNIFIED_PATHS =
      LFS_OBJECTS_PATH + "|" + LFS_LOCKS_PATH + "|" + LFS_VERIFICATION_PATH;
  public static final String URL_REGEX_TEMPLATE =
      "^(?:/a)?(?:/p/|/)(.+)(?:/info/lfs/)(?:%s)$";

  public static final String URL_REGEX =
      String.format(URL_REGEX_TEMPLATE, LFS_UNIFIED_PATHS);

  private LfsPaths() {}
}
