//Copyright (C) 2016 The Android Open Source Project
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.googlesource.gerrit.plugins.lfs.fs;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.project.ProjectCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.LfsApiServlet;

import org.eclipse.jgit.lfs.server.LargeFileRepository;

@Singleton
public class LfsFsApiServlet extends LfsApiServlet {
  private static final long serialVersionUID = 1L;

  private final LocalLargeFileRepository repository;

  @Inject
  LfsFsApiServlet(@PluginName String pluginName,
      PluginConfigFactory pluginConfigFactory,
      ProjectCache projectCache,
      LocalLargeFileRepository repository) {
    super(pluginName, pluginConfigFactory, projectCache);
    this.repository = repository;
  }

  @Override
  protected LargeFileRepository getLargeFileRepository() {
    return repository;
  }
}
