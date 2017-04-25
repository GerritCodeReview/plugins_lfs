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

public class LfsListLocksInput {
  public final String path;
  public final String id;
  public final String cursor;
  public final Integer limit;

  LfsListLocksInput(String path, String id, String cursor, Integer limit) {
    this.path = path;
    this.id = id;
    this.cursor = cursor;
    this.limit = limit;
  }
}
