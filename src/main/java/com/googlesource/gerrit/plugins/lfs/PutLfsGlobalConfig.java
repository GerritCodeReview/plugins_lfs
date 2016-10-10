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

import static com.googlesource.gerrit.plugins.lfs.LfsProjectConfigSection.KEY_BACKEND;
import static com.googlesource.gerrit.plugins.lfs.LfsProjectConfigSection.KEY_ENABLED;
import static com.googlesource.gerrit.plugins.lfs.LfsProjectConfigSection.KEY_MAX_OBJECT_SIZE;
import static com.googlesource.gerrit.plugins.lfs.LfsProjectConfigSection.KEY_READ_ONLY;

import com.google.common.base.Strings;
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
import java.util.Map.Entry;
import java.util.Set;

@Singleton
class PutLfsGlobalConfig
    implements RestModifyView<ProjectResource, LfsGlobalConfigInput> {

  private final String pluginName;
  private final AllProjectsName allProjectsName;
  private final Provider<CurrentUser> self;
  private final Provider<MetaDataUpdate.User> metaDataUpdateFactory;
  private final LfsConfigurationFactory lfsConfigFactory;
  private final GetLfsGlobalConfig get;

  @Inject
  PutLfsGlobalConfig(@PluginName String pluginName,
      AllProjectsName allProjectsName,
      Provider<CurrentUser> self,
      Provider<MetaDataUpdate.User> metaDataUpdateFactory,
      LfsConfigurationFactory lfsConfigFactory,
      GetLfsGlobalConfig get) {
    this.pluginName = pluginName;
    this.allProjectsName = allProjectsName;
    this.self = self;
    this.metaDataUpdateFactory = metaDataUpdateFactory;
    this.lfsConfigFactory = lfsConfigFactory;
    this.get = get;
  }

  @Override
  public LfsGlobalConfigInfo apply(ProjectResource resource,
      LfsGlobalConfigInput input) throws RestApiException {
    IdentifiedUser user = self.get().asIdentifiedUser();
    Project.NameKey projectName = resource.getNameKey();

    if (!(projectName.equals(allProjectsName)
        && user.getCapabilities().canAdministrateServer())) {
      throw new ResourceNotFoundException();
    }

    if (input == null) {
      input = new LfsGlobalConfigInput();
    }

    LfsProjectsConfig config = lfsConfigFactory.getProjectsConfig();
    try (MetaDataUpdate md = metaDataUpdateFactory.get().create(projectName)) {
      try {
        config.load(md);
      } catch (ConfigInvalidException | IOException e) {
        throw new ResourceConflictException(
            "Cannot read LFS config in " + projectName);
      }
      Config cfg = new Config();
      if (input.namespaces != null) {
        Set<String> backends =
            lfsConfigFactory.getGlobalConfig().getBackends().keySet();
        Set<Entry<String, LfsProjectConfigInfo>> namespaces =
            input.namespaces.entrySet();
        for (Map.Entry<String, LfsProjectConfigInfo> namespace : namespaces) {
          LfsProjectConfigInfo info = namespace.getValue();
          if (info.enabled != null) {
            cfg.setBoolean(
                pluginName, namespace.getKey(), KEY_ENABLED, info.enabled);
          }
          if (info.maxObjectSize != null) {
            cfg.setLong(
                pluginName, namespace.getKey(),
                KEY_MAX_OBJECT_SIZE, info.maxObjectSize);
          }
          if (info.readOnly != null) {
            cfg.setBoolean(
                pluginName, namespace.getKey(), KEY_READ_ONLY, info.readOnly);
          }
          if (!Strings.isNullOrEmpty(info.backend)) {
            if (!backends.contains(info.backend)) {
              throw new ResourceConflictException(
                  String.format("Namespace %s: backend %s does not exist",
                      namespace, info.backend));
            }
            cfg.setString(pluginName, namespace.getKey(),
                KEY_BACKEND, info.backend);
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
