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

import com.google.gerrit.server.index.IndexConfig;
import com.google.gerrit.server.query.InternalQuery;
import com.google.gerrit.server.query.Predicate;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.events.LfsData;
import com.googlesource.gerrit.plugins.lfs.index.LfsObjectsIndexCollection;

import java.util.List;

public class InternalLfsQuery extends InternalQuery<LfsData> {

  @Inject
  protected InternalLfsQuery(LfsQueryProcessor queryProcessor,
      LfsObjectsIndexCollection indexes,
      IndexConfig indexConfig) {
    super(queryProcessor, indexes, indexConfig);
  }

  public List<LfsData> byKey(String key) throws OrmException {
    return query(key(key));
  }

  public List<LfsData> byProject(String project) throws OrmException {
    return query(project(project));
  }

  private Predicate<LfsData> key(String key) {
    return new KeyPredicate(key);
  }

  private Predicate<LfsData> project(String project) {
    return new ProjectPredicate(project);
  }
}
