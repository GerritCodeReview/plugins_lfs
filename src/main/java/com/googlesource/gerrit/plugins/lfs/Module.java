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

import com.google.gerrit.extensions.annotations.Exports;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.extensions.client.InheritableBoolean;
import com.google.gerrit.server.config.ProjectConfigEntry;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository;
import com.googlesource.gerrit.plugins.lfs.s3.S3LargeFileRepository;

import org.eclipse.jgit.lfs.server.fs.LfsFsContentServlet;

import java.util.ArrayList;
import java.util.Set;

public class Module extends FactoryModule {

  public final static String KEY_BACKEND           = "backend";
  public final static String VALUE_BACKEND_NONE    = "None";
  public final static String VALUE_BACKEND_DEFAULT = "Default";
  public final static String KEY_WRITABLE          = "writable";
  public final static String KEY_MAX_OBJECT_SIZE   = "maxObjectSize";

  private final Set<String> backends;

  @Inject
  Module(LfsConfigurationFactory configFactory) {
    this.backends = configFactory.getGlobalConfig().getBackends().keySet();
  }

  @Override
  protected void configure() {
    factory(S3LargeFileRepository.Factory.class);
    factory(LocalLargeFileRepository.Factory.class);
    factory(LfsFsContentServlet.Factory.class);

    // Prepare the list of backend Types. Allow it to be set to 'None' as well
    // to disable the whole feature for this Git repo. Default is also 'None'
    final ArrayList listBackend = new ArrayList<>();
    listBackend.add(VALUE_BACKEND_NONE);
    listBackend.add(VALUE_BACKEND_DEFAULT);
    listBackend.addAll(backends);
    bind(ProjectConfigEntry.class)
        .annotatedWith(Exports.named(KEY_BACKEND))
        .toInstance(new ProjectConfigEntry("Backend", VALUE_BACKEND_NONE,
            listBackend, true,
            "Backend options are defined in $GERRIT_SITE/etc/lfs.config."));

    bind(ProjectConfigEntry.class)
        .annotatedWith(Exports.named(KEY_WRITABLE))
        .toInstance(new ProjectConfigEntry("Write Enabled",
            InheritableBoolean.TRUE,
            InheritableBoolean.class, true,
            "If 'false' reading LFS objects is still possible but pushing " +
            "is forbidden. Note that regular git operations are not " +
            "affected."));

    bind(ProjectConfigEntry.class)
        .annotatedWith(Exports.named(KEY_MAX_OBJECT_SIZE))
        .toInstance(new ProjectConfigEntry("Maximum Object Size", 0, true,
        "Maximum allowed object size (per object) in bytes for projects in " +
        "this namespace, or 0 for no limit. " +
        "Common unit suffixes of k, m, and g are supported."));
  }
}
