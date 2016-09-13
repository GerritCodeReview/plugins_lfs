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

import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.server.project.ProjectResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class GetLfsConfig implements RestReadView<ProjectResource> {

  private final LfsConfig.Factory lfsConfigFactory;

  @Inject
  GetLfsConfig(LfsConfig.Factory lfsConfigFactory) {
    this.lfsConfigFactory = lfsConfigFactory;
  }

  @Override
  public LfsConfigInfo apply(ProjectResource resource) throws RestApiException {
    LfsConfigInfo info = new LfsConfigInfo();
    LfsConfigSection config =
        lfsConfigFactory.create().getForProject(resource.getNameKey());
    if (config != null) {
      info.enabled = config.isEnabled();
      info.maxObjectSize = config.getMaxObjectSize();
    }
    return info;
  }
}
