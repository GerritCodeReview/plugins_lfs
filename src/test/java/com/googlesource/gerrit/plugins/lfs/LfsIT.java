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

import com.google.gerrit.acceptance.GerritConfig;
import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.TestPlugin;
import com.google.gerrit.reviewdb.client.Project;
import org.junit.Test;

@TestPlugin(
    name = "lfs",
    sysModule = "com.googlesource.gerrit.plugins.lfs.Module",
    httpModule = "com.googlesource.gerrit.plugins.lfs.HttpModule",
    sshModule = "com.googlesource.gerrit.plugins.lfs.SshModule")
public class LfsIT extends LightweightPluginDaemonTest {
  @Test
  @GerritConfig(name = "lfs.plugin", value = "lfs")
  public void globalConfigCanBeReadByAdmin() throws Exception {
    adminRestSession.get(globalConfig(allProjects)).assertOK();
  }

  @Test
  @GerritConfig(name = "lfs.plugin", value = "lfs")
  public void globalConfigCannotBeReadByNonAdmin() throws Exception {
    userRestSession.get(globalConfig(allProjects)).assertNotFound();
  }

  @Test
  @GerritConfig(name = "lfs.plugin", value = "lfs")
  public void globalConfigCannotBeReadOnOtherProject() throws Exception {
    adminRestSession.get(globalConfig(project)).assertNotFound();
  }

  private static String globalConfig(Project.NameKey name) {
    return String.format("/projects/%s/lfs:config-global", name.get());
  }
}
