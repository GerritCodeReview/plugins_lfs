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

import static org.eclipse.jgit.transport.ReceiveCommand.Type.CREATE;
import static org.eclipse.jgit.transport.ReceiveCommand.Type.DELETE;
import static org.eclipse.jgit.transport.ReceiveCommand.Type.UPDATE;
import static org.eclipse.jgit.transport.ReceiveCommand.Type.UPDATE_NONFASTFORWARD;

import com.google.common.base.Function;
import com.google.gerrit.server.data.RefUpdateAttribute;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class RefUpdateToOperationConverter
  implements Function<RefUpdateAttribute, ReceiveCommand.Type> {

  interface Factory {
    RefUpdateToOperationConverter create(Repository repo);
  }

  private static final Logger log =
      LoggerFactory.getLogger(RefUpdateToOperationConverter.class);

  private final Repository repo;

  @Inject
  RefUpdateToOperationConverter(@Assisted Repository repo) {
    this.repo = repo;
  }

  @Override
  public ReceiveCommand.Type apply(RefUpdateAttribute attrib) {
    if (ObjectId.zeroId().getName().equals(attrib.newRev)) {
      return DELETE;
    }

    if (ObjectId.zeroId().getName().equals(attrib.oldRev)) {
      return CREATE;
    }

    if (attrib.newRev.equals(attrib.oldRev)) {
      return UPDATE;
    }

    try (RevWalk walk = new RevWalk(repo)) {
      walk.setRetainBody(false);
      RevObject oldRevObj = walk.parseAny(ObjectId.fromString(attrib.oldRev));
      RevObject newRevObj = walk.parseAny(ObjectId.fromString(attrib.newRev));
      if (!(oldRevObj instanceof RevCommit)
          || !(newRevObj instanceof RevCommit)
          || !walk.isMergedInto((RevCommit) oldRevObj, (RevCommit) newRevObj)) {
        return UPDATE_NONFASTFORWARD;
      }
      return UPDATE;
    } catch (IOException e) {
      log.error(String.format("Exception was thrown while oldRev: %s and newRev: %s were parsed",
          attrib.oldRev, attrib.newRev), e);
      return UPDATE_NONFASTFORWARD;
    }
  }
}
