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

import com.google.common.collect.Maps;
import com.google.gerrit.extensions.restapi.Response;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.server.project.ProjectResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;

@Singleton
class GetLfsGlobalConfig implements RestReadView<ProjectResource> {
  private final LfsConfigurationFactory lfsConfigFactory;
  private final LfsAdminView adminView;

  @Inject
  GetLfsGlobalConfig(LfsConfigurationFactory lfsConfigFactory, LfsAdminView adminView) {
    this.lfsConfigFactory = lfsConfigFactory;
    this.adminView = adminView;
  }

  @Override
  public Response<LfsGlobalConfigInfo> apply(ProjectResource resource) throws RestApiException {
    adminView.validate(resource);

    LfsGlobalConfigInfo info = new LfsGlobalConfigInfo();
    LfsGlobalConfig globalConfig = lfsConfigFactory.getGlobalConfig();
    info.defaultBackendType = globalConfig.getDefaultBackend().type;
    info.backends = Maps.transformValues(globalConfig.getBackends(), b -> b.type);

    List<LfsProjectConfigSection> configSections =
        lfsConfigFactory.getProjectsConfig().getConfigSections();
    if (!configSections.isEmpty()) {
      info.namespaces = new HashMap<>(configSections.size());
      for (LfsProjectConfigSection section : configSections) {
        LfsProjectConfigInfo sectionInfo = new LfsProjectConfigInfo();
        sectionInfo.enabled = section.isEnabled();
        sectionInfo.maxObjectSize = section.getMaxObjectSize();
        sectionInfo.readOnly = section.isReadOnly();
        sectionInfo.backend = section.getBackend();
        info.namespaces.put(section.getNamespace(), sectionInfo);
      }
    }
    return Response.ok(info);
  }
}
