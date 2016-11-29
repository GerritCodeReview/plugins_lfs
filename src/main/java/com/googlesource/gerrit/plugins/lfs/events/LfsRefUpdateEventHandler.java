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

package com.googlesource.gerrit.plugins.lfs.events;

import static com.google.gerrit.reviewdb.client.RefNames.REFS_META;

import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsProjectConfigSection;
import com.googlesource.gerrit.plugins.lfs.query.ReindexLfsObject;

import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

class LfsRefUpdateEventHandler implements GitReferenceUpdatedListener {
  private static final Logger log =
      LoggerFactory.getLogger(LfsRefUpdateEventHandler.class);

  private final LfsConfigurationFactory lfsConfigFactory;
  private final GitRepositoryManager repoyMgr;
  private final LfsDataProvider.Factory toLfsData;
  private final ReindexLfsObject reindexer;

  @Inject
  public LfsRefUpdateEventHandler(LfsConfigurationFactory lfsConfigFactory,
      GitRepositoryManager repoyMgr,
      LfsDataProvider.Factory toLfsData,
      ReindexLfsObject reindexer) {
    this.lfsConfigFactory = lfsConfigFactory;
    this.repoyMgr = repoyMgr;
    this.toLfsData = toLfsData;
    this.reindexer = reindexer;
  }

  @Override
  public void onGitReferenceUpdated(Event event) {
    Project.NameKey project = new Project.NameKey(event.getProjectName());
    LfsProjectConfigSection cfg = lfsConfigFactory.getProjectsConfig()
        .getForProject(project);
    if ((cfg == null) || (!cfg.isEnabled())
        || (isNotInteresting(event))) {
      return;
    }

    try(Repository repo = repoyMgr.openRepository(project)) {
      if (event.isDelete()) {
        //TODO: handle delete case
      } else if (event.isNonFastForward()) {
        //TODO: requires special handling for oldRev
      } else {
        //handle create/update
        List<LfsData> lfs = toLfsData.create(event.getProjectName(), repo)
            .apply(event.getOldObjectId(), event.getNewObjectId());
        reindexer.reindex(lfs);
        log.debug("Size of LFS objects list {}", lfs.size());
      }
    } catch (IOException e) {
      log.error("Excpetion while handling refUpdatedEvent", e);
    }
  }

  private boolean isNotInteresting(Event event) {
    return event.getRefName().startsWith(REFS_META);
  }
}
