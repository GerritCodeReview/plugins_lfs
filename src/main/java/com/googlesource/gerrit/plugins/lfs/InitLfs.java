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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.pgm.init.api.InitStep;
import com.google.gerrit.pgm.init.api.Section;
import com.google.inject.Inject;

public class InitLfs implements InitStep {
  private static final String LFS_SECTION = "lfs";
  private static final String PLUGIN_KEY = "plugin";

  private final String name;
  private final Section lfs;

  @Inject
  InitLfs(@PluginName String name, Section.Factory sections) {
    this.name = name;
    this.lfs = sections.get(LFS_SECTION, null);
  }

  @Override
  public void run() throws Exception {
    lfs.set(PLUGIN_KEY, name);
  }
}
