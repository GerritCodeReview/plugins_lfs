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

package com.googlesource.gerrit.plugins.lfs.index;

import com.google.gerrit.server.index.Index;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.events.LfsData;

import java.io.IOException;
import java.util.Collection;

public class LfsObjectsIndexer {
  interface Factory {
    LfsObjectsIndexer create(LfsObjectsIndexCollection indexes);
  }

  private final LfsObjectsIndexCollection indexes;

  @Inject
  LfsObjectsIndexer(LfsObjectsIndexCollection indexes) {
    this.indexes = indexes;
  }

  public void index(LfsData lfs) throws IOException {
    for (Index<?, LfsData> i : indexes.getWriteIndexes()) {
      i.replace(lfs);
    }
  }

  public void index(Collection<LfsData> lfsData) throws IOException {
    for (LfsData lfs : lfsData) {
      index(lfs);
    }
  }
}
