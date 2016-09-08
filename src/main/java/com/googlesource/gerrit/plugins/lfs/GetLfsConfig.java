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
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.config.AllProjectsName;
import com.google.gerrit.server.project.ProjectResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.List;

@Singleton
class GetLfsConfig implements RestReadView<ProjectResource> {

  private final LfsConfig lfsConfig;
  private final AllProjectsName allProjectsName;

  @Inject
  GetLfsConfig(LfsConfig lfsConfig,
      AllProjectsName allProjectsName) {
    this.lfsConfig = lfsConfig;
    this.allProjectsName = allProjectsName;
  }

  @Override
  public LfsConfigInfo apply(ProjectResource resource) throws RestApiException {
    IdentifiedUser user = resource.getControl().getUser().asIdentifiedUser();
    if (resource.getNameKey().equals(allProjectsName)
        && user.getCapabilities().canAdministrateServer()) {
      LfsGlobalConfigInfo info = new LfsGlobalConfigInfo();
      info.backend = lfsConfig.getBackend();
      List<LfsConfigSection> configSections = lfsConfig.getConfigSections();
      if (!configSections.isEmpty()) {
        info.namespaces = new HashMap<>();
        for (LfsConfigSection section : configSections) {
          LfsProjectConfigInfo sectionInfo = new LfsProjectConfigInfo();
          sectionInfo.enabled = section.isEnabled();
          sectionInfo.maxObjectSize = section.getMaxObjectSize();
          info.namespaces.put(section.getNamespace(), sectionInfo);
        }
      }
      return info;
    }
    LfsProjectConfigInfo info = new LfsProjectConfigInfo();
    LfsConfigSection config = lfsConfig.getForProject(resource.getNameKey());
    if (config != null) {
      info.enabled = config.isEnabled();
      info.maxObjectSize = config.getMaxObjectSize();
    }
    return info;
  }
}
