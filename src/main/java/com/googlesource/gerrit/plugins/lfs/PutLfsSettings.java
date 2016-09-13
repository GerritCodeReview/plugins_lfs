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

import static com.googlesource.gerrit.plugins.lfs.LfsConfigSection.KEY_ENABLED;
import static com.googlesource.gerrit.plugins.lfs.LfsConfigSection.KEY_MAX_OBJECT_SIZE;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.restapi.ResourceConflictException;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.config.AllProjectsName;
import com.google.gerrit.server.git.MetaDataUpdate;
import com.google.gerrit.server.project.ProjectResource;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Config;

import java.io.IOException;
import java.util.Map;

@Singleton
class PutLfsSettings implements RestModifyView<ProjectResource, LfsSettingsInput> {

  private final String pluginName;
  private final AllProjectsName allProjectsName;
  private final Provider<CurrentUser> self;
  private final Provider<MetaDataUpdate.User> metaDataUpdateFactory;
  private final LfsConfig.Factory lfsConfigFactory;
  private final GetLfsSettings get;

  @Inject
  PutLfsSettings(@PluginName String pluginName,
      AllProjectsName allProjectsName,
      Provider<CurrentUser> self,
      Provider<MetaDataUpdate.User> metaDataUpdateFactory,
      LfsConfig.Factory lfsConfigFactory,
      GetLfsSettings get) {
    this.pluginName = pluginName;
    this.allProjectsName = allProjectsName;
    this.self = self;
    this.metaDataUpdateFactory = metaDataUpdateFactory;
    this.lfsConfigFactory = lfsConfigFactory;
    this.get = get;
  }

  @Override
  public LfsSettingsInfo apply(ProjectResource resource, LfsSettingsInput input)
      throws RestApiException {
    IdentifiedUser user = self.get().asIdentifiedUser();
    Project.NameKey projectName = resource.getNameKey();

    if (!(projectName.equals(allProjectsName)
        && user.getCapabilities().canAdministrateServer())) {
      throw new ResourceNotFoundException();
    }

    if (input == null) {
      input = new LfsSettingsInput();
    }

    LfsConfig config = lfsConfigFactory.create(projectName);
    Config cfg = new Config();
    try (MetaDataUpdate md = metaDataUpdateFactory.get().create(projectName)) {
      try {
        config.load(md);
      } catch (ConfigInvalidException | IOException e) {
        throw new ResourceConflictException(
            "Cannot read LFS config in " + projectName);
      }
      if (input.namespaces != null) {
        for (Map.Entry<String, LfsConfigInfo> e : input.namespaces.entrySet()) {
          LfsConfigInfo info = e.getValue();
          if (info.enabled != null) {
            cfg.setBoolean(pluginName, e.getKey(), KEY_ENABLED, info.enabled);
          }
          if (info.maxObjectSize != null) {
            cfg.setLong(
                pluginName, e.getKey(), KEY_MAX_OBJECT_SIZE, info.maxObjectSize);
          }
        }
      }
      config.setProjectConfig(cfg);
      try {
        config.commit(md);
      } catch (IOException e) {
        if (e.getCause() instanceof ConfigInvalidException) {
          throw new ResourceConflictException(
              "Cannot update LFS config in " + projectName
              + ": " + e.getCause().getMessage());
        }
        throw new ResourceConflictException(
            "Cannot update LFS config in " + projectName);
      }
    } catch (RepositoryNotFoundException e) {
      throw new ResourceNotFoundException(projectName.get());
    } catch (IOException e) {
      throw new ResourceNotFoundException(projectName.get(), e);
    }

    return get.apply(resource);
  }
}
