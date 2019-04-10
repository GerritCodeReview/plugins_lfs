// Copyright (C) 2019 The Android Open Source Project
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

import static com.google.gerrit.server.permissions.GlobalPermission.ADMINISTRATE_SERVER;

import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.config.AllProjectsName;
import com.google.gerrit.server.permissions.PermissionBackend;
import com.google.gerrit.server.project.ProjectResource;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class LfsAdminView {
  private final AllProjectsName allProjectsName;
  private final PermissionBackend permissionBackend;
  private final Provider<CurrentUser> self;

  @Inject
  LfsAdminView(
      AllProjectsName allProjectsName,
      PermissionBackend permissionBackend,
      Provider<CurrentUser> self) {
    this.allProjectsName = allProjectsName;
    this.permissionBackend = permissionBackend;
    this.self = self;
  }

  /**
   * Validate REST call.
   *
   * @param resource the resource
   * @throws ResourceNotFoundException if the calling user is not admin, or the resource is not
   *     {@code All-Projects}.
   */
  public void validate(ProjectResource resource) throws ResourceNotFoundException {
    if (!(resource.getNameKey().equals(allProjectsName)
        && permissionBackend.user(self.get()).testOrFalse(ADMINISTRATE_SERVER))) {
      throw new ResourceNotFoundException();
    }
  }
}
