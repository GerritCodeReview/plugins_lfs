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

import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.index.IndexConfig;
import com.google.gerrit.server.query.Predicate;
import com.google.gerrit.server.query.QueryProcessor;
import com.google.inject.Inject;
import com.google.inject.Provider;

import com.googlesource.gerrit.plugins.lfs.events.LfsData;
import com.googlesource.gerrit.plugins.lfs.index.LfsObjectsIndexCollection;
import com.googlesource.gerrit.plugins.lfs.index.LfsObjectsIndexRewriter;
import com.googlesource.gerrit.plugins.lfs.index.LfsObjectsSchemaDefinitions;

public class LfsQueryProcessor extends QueryProcessor<LfsData> {

  @Inject
  protected LfsQueryProcessor(Provider<CurrentUser> userProvider,
      QueryProcessor.Metrics metrics,
      LfsObjectsSchemaDefinitions schemaDef,
      IndexConfig indexConfig,
      LfsObjectsIndexCollection indexes,
      LfsObjectsIndexRewriter rewriter) {
    super(userProvider, metrics, schemaDef, indexConfig, indexes, rewriter,
        null);
  }

  @Override
  protected Predicate<LfsData> enforceVisibility(Predicate<LfsData> pred) {
    return pred;
  }
}
