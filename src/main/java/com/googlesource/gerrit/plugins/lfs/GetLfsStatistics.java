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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gerrit.extensions.restapi.ResourceConflictException;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.server.project.ProjectResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.lfs.fs.LfsFsDataDirectoryManager;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class GetLfsStatistics implements RestReadView<ProjectResource> {
  private final LfsFsDataDirectoryManager dataManager;
  private final LfsConfigurationFactory lfsConfigFactory;
  private final LfsAdminView adminView;

  @Inject
  GetLfsStatistics(
      LfsFsDataDirectoryManager dataManager,
      LfsConfigurationFactory lfsConfigFactory,
      LfsAdminView adminView) {
    this.dataManager = dataManager;
    this.lfsConfigFactory = lfsConfigFactory;
    this.adminView = adminView;
  }

  private static class StatisticsProcessor extends SimpleFileVisitor<Path> {
    private final Path path;
    private LfsStatisticsInfo info;

    StatisticsProcessor(Path path) {
      this.path = path;
    }

    public LfsStatisticsInfo process() throws IOException {
      info = new LfsStatisticsInfo();
      info.totalSize = 0;
      info.totalFiles = 0;
      info.largestFileSize = 0;
      Files.walkFileTree(path, this);
      return info;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
      info.totalFiles++;
      info.totalSize += attr.size();
      info.largestFileSize = Math.max(info.largestFileSize, attr.size());
      return FileVisitResult.CONTINUE;
    }
  }

  @Override
  public Map<String, LfsStatisticsInfo> apply(ProjectResource resource) throws RestApiException {
    adminView.apply(resource);

    Map<String, LfsStatisticsInfo> result = new HashMap<>();
    LfsGlobalConfig globalConfig = lfsConfigFactory.getGlobalConfig();

    // Map storage paths to the backends that use the paths
    ListMultimap<Path, String> storageLocations = ArrayListMultimap.create();

    try {
      LfsBackend defaultBackend = globalConfig.getDefaultBackend();
      storageLocations.put(dataManager.getForBackend(defaultBackend, false), defaultBackend.name());

      Map<String, LfsBackend> backends = globalConfig.getBackends();
      for (String name : backends.keySet()) {
        LfsBackend backend = backends.get(name);
        storageLocations.put(dataManager.getForBackend(backend, false), backend.name());
      }

      for (Path path : storageLocations.keySet()) {
        if (Files.exists(path)) {
          LfsStatisticsInfo info = new StatisticsProcessor(path).process();
          info.backends = storageLocations.get(path);
          result.put(path.toString(), info);
        }
      }
      return result;
    } catch (IOException e) {
      throw new ResourceConflictException("Cannot get LFS statistics");
    }
  }
}
