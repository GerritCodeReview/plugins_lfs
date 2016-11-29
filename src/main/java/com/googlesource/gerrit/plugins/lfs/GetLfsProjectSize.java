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

import static com.googlesource.gerrit.plugins.lfs.LfsProjectSizeInfo.EMPTY;

import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.BadRequestException;
import com.google.gerrit.extensions.restapi.ResourceConflictException;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.server.project.ProjectResource;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.events.LfsData;
import com.googlesource.gerrit.plugins.lfs.query.InternalLfsQuery;

import java.util.Iterator;
import java.util.List;

@Singleton
public class GetLfsProjectSize implements RestReadView<ProjectResource> {
  private final Provider<InternalLfsQuery> queryProvider;

  @Inject
  GetLfsProjectSize(Provider<InternalLfsQuery> queryProvider) {
    this.queryProvider = queryProvider;
  }

  @Override
  public LfsProjectSizeInfo apply(ProjectResource resource)
      throws AuthException, BadRequestException, ResourceConflictException,
      Exception {
    String project = resource.getNameKey().get();
    List<LfsData> lfs = queryProvider.get().byProject(project);
    if (lfs.isEmpty()) {
      return EMPTY;
    }

    LfsProjectSizeInfo size = new LfsProjectSizeInfo();
    for (LfsData lfsData : lfs) {
      Iterator<String> it = lfsData.projects.iterator();
      if (it.hasNext()) {
        it.next();
        // if there is more projects on the list it means that object
        // is shared between multiple projects
        if (it.hasNext()) {
          size.shared += lfsData.size;
        } else {
          size.exclusive += lfsData.size;
        }
      }
    }
    size.all = size.exclusive + size.shared;
    return size;
  }
}
