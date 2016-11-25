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

import static com.google.gerrit.reviewdb.client.RefNames.REFS;

import com.google.gerrit.server.data.RefUpdateAttribute;
import com.google.gerrit.server.events.RefUpdatedEvent;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsProjectConfigSection;
import com.googlesource.gerrit.plugins.lfs.index.LfsObjectsIndexer;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

class LfsRefUpdateEventHandler {
  private static final Logger log =
      LoggerFactory.getLogger(LfsRefUpdateEventHandler.class);
  private static final String REFS_META = REFS + "meta/";

  private final LfsConfigurationFactory lfsConfigFactory;
  private final GitRepositoryManager repoyMgr;
  private final RefUpdateToOperationConverter.Factory toOperation;
  private final LfsDataProvider.Factory toLfsData;
  private final LfsObjectsIndexer indexer;

  @Inject
  public LfsRefUpdateEventHandler(LfsConfigurationFactory lfsConfigFactory,
      GitRepositoryManager repoyMgr,
      RefUpdateToOperationConverter.Factory toOperation,
      LfsDataProvider.Factory toLfsData,
      LfsObjectsIndexer indexer) {
    this.lfsConfigFactory = lfsConfigFactory;
    this.repoyMgr = repoyMgr;
    this.toOperation = toOperation;
    this.toLfsData = toLfsData;
    this.indexer = indexer;
  }

  void handle(RefUpdatedEvent event) {
    LfsProjectConfigSection cfg = lfsConfigFactory.getProjectsConfig()
        .getForProject(event.getProjectNameKey());
    RefUpdateAttribute refUpdate = event.refUpdate.get();
    if ((cfg == null) || (!cfg.isEnabled())
        || (isNotInteresting(refUpdate))) {
      return;
    }

    try(Repository repo = repoyMgr.openRepository(event.getProjectNameKey())) {
      ReceiveCommand.Type op = toOperation.create(repo).apply(refUpdate);
      switch (op) {
        case DELETE:
          //TODO: handle delete case
          break;

        case UPDATE_NONFASTFORWARD:
          //TODO: requires special handling for oldRev
          break;

        case CREATE:
        case UPDATE:
          //handle these cases together
          List<LfsData> lfs = toLfsData.create(refUpdate.project, repo)
              .apply(refUpdate.oldRev, refUpdate.newRev);
          indexer.index(lfs);
          log.info("Size of LFS objects list {}", lfs.size());
          break;

        default:
          log.info("Unhandled operation type {}", op);
      }
    } catch (IOException e) {
      log.error("Excpetion while handling refUpdatedEvent", e);
    }
  }

  private boolean isNotInteresting(RefUpdateAttribute refUpdate) {
    return refUpdate.refName.startsWith(REFS_META);
  }
}
