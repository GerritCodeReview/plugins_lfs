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

import com.google.common.base.Stopwatch;
import com.google.gerrit.server.index.SiteIndexer;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.events.LfsData;

@Singleton
public class AllObjectsIndexer
  extends SiteIndexer<String, LfsData, LfsObjectsIndex> {

  @Override
  public SiteIndexer.Result indexAll(
      LfsObjectsIndex index) {
    // TODO re-index all backends but for now do nothing :)
    Stopwatch sw = Stopwatch.createStarted();
    return new SiteIndexer.Result(sw, true, 0, 0);
  }
}
