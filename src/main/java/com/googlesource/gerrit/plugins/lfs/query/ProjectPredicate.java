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

package com.googlesource.gerrit.plugins.lfs.query;

import static com.googlesource.gerrit.plugins.lfs.index.LfsFields.PROJECT;

import com.google.common.collect.FluentIterable;
import com.google.gwtorm.server.OrmException;

import com.googlesource.gerrit.plugins.lfs.events.LfsData;

public class ProjectPredicate extends LfsIndexPredicate {
  ProjectPredicate(String value) {
    super(PROJECT, value);
  }

  @Override
  public boolean match(LfsData object) throws OrmException {
    if (object == null) {
      return false;
    }

    return FluentIterable.from(object.projects)
        .contains(getValue());
  }

  @Override
  public int getCost() {
    return 1;
  }
}
