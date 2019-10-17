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

import com.google.common.base.Strings;
import com.google.common.flogger.FluentLogger;
import com.google.gerrit.entities.Project;
import com.google.inject.Inject;
import java.util.Map;
import org.eclipse.jgit.lfs.errors.LfsRepositoryNotFound;
import org.eclipse.jgit.lfs.server.LargeFileRepository;

public class LfsRepositoryResolver {
  private static final FluentLogger log = FluentLogger.forEnclosingClass();

  private final LfsRepositoriesCache cache;
  private final LfsBackend defaultBackend;
  private final Map<String, LfsBackend> backends;

  @Inject
  LfsRepositoryResolver(LfsRepositoriesCache cache, LfsConfigurationFactory configFactory) {
    this.cache = cache;

    LfsGlobalConfig config = configFactory.getGlobalConfig();
    this.defaultBackend = config.getDefaultBackend();
    this.backends = config.getBackends();
  }

  public LargeFileRepository get(Project.NameKey project, String backendName)
      throws LfsRepositoryNotFound {
    LfsBackend backend;
    if (Strings.isNullOrEmpty(backendName)) {
      backend = defaultBackend;
    } else {
      backend = backends.get(backendName);
      if (backend == null) {
        log.atSevere().log(
            "Project %s is configured with not existing backend %s", project, backendName);
        throw new LfsRepositoryNotFound(project.get());
      }
    }

    LargeFileRepository repository = cache.get(backend);
    if (repository != null) {
      return repository;
    }

    // this is unlikely situation as cache is pre-populated from config but...
    log.atSevere().log(
        "Project %s is configured with not existing backend %s of type %s",
        project, backend.name(), backend.type);
    throw new LfsRepositoryNotFound(project.get());
  }
}
