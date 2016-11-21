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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.jgit.attributes.Attribute;
import org.eclipse.jgit.attributes.Attributes;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.io.AutoLFInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

class LfsDataProvider {
  interface Factory {
    LfsDataProvider create(String project, Repository repo);
  }

  private static final Logger log =
      LoggerFactory.getLogger(LfsDataProvider.class);
  private static final String OID_PREFIX = "oid sha256:";
  private static final String SIZE_PREFIX = "size ";

  private final ProjectToBackendCache backends;
  private final Repository repo;
  private final String project;

  @Inject
  LfsDataProvider(ProjectToBackendCache backends,
      @Assisted String project,
      @Assisted Repository repo) {
    this.backends = backends;
    this.project = project;
    this.repo = repo;
  }

  List<LfsData> apply(String oldRev, String newRev) {
    try (RevWalk rw = new RevWalk(repo)) {
      rw.markStart(rw.parseCommit(repo.resolve(newRev)));
      if (!ObjectId.zeroId().getName().equals(oldRev)) {
        rw.markUninteresting(rw.parseCommit(repo.resolve(oldRev)));
      }

      return FluentIterable.from(rw)
          .transformAndConcat(new Function<RevCommit, List<LfsData>>() {
        @Override
        public List<LfsData> apply(RevCommit c) {
          if (c.getParentCount() > 1) {
            return Collections.emptyList();
          }

          try (TreeWalk tw = new TreeWalk(repo)) {
            ImmutableList.Builder<LfsData> list =
                ImmutableList.<LfsData>builder();
            tw.setRecursive(true);
            tw.setFilter(TreeFilter.ANY_DIFF);
            tw.addTree(c.getTree());
            while (tw.next()) {
              Optional<LfsData> lfs = processEntry(c, tw, rw);
              if (lfs.isPresent()) {
                list.add(lfs.get());
              }
            }
            return list.build();
          } catch (IOException e) {
            log.error("Reading LFS data for {} in project {} failed",
                c.getName(), project, e);
          }
          return Collections.emptyList();
        }
        }).toList();
    } catch (RevisionSyntaxException | IOException e) {
      log.error("Reading LFS data in project {} failed", project, e);
    }
    return Collections.emptyList();
  }

  private Optional<LfsData> processEntry(RevCommit c, TreeWalk tw, RevWalk rw)
      throws IOException {
    Attributes attributes = tw.getAttributes();
    Attribute attribute = attributes.get("filter");
    if ((attribute == null)
        || !"lfs".equalsIgnoreCase(attribute.getValue())) {
      return Optional.absent();
    }

    FileMode mode = tw.getFileMode(0);
    if (FileMode.GITLINK == mode) {
      return Optional.absent();
    }

    ObjectId objId = tw.getObjectId(0);
    RevObject ro = rw.lookupAny(objId, mode.getObjectType());
    rw.parseBody(ro);
    ObjectLoader ldr = repo.open(ro.getId(), Constants.OBJ_BLOB);
    try (AutoLFInputStream is =
            new AutoLFInputStream(ldr.openStream(), true, true);
        BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      LfsData.Builder builder = readLfs(br);
      if (builder.isValid()) {
        return Optional.of(builder.withCommit(c.name())
            .withProject(project)
            .withBackend(backends.get(project).displayName)
            .build());
      }
    } catch (AutoLFInputStream.IsBinaryException e) {
      // it means that this is binary file so it doesn't have to processed
    }

    return Optional.absent();
  }

  private LfsData.Builder readLfs(BufferedReader br) throws IOException {
    LfsData.Builder builder = new LfsData.Builder();

    // according to https://github.com/git-lfs/git-lfs/blob/master/docs/spec.md
    // version is always first line
    String line = br.readLine();
    if (Strings.isNullOrEmpty(line) || !line.startsWith("version ")
        || !line.contains("git-lfs")) {
      return builder;
    }

    while ((line = br.readLine()) != null) {
      if (line.startsWith(OID_PREFIX) && line.length() > OID_PREFIX.length()) {
        builder.withOid(line.substring(OID_PREFIX.length()));
      } else if (line.startsWith(SIZE_PREFIX)
          && line.length() > SIZE_PREFIX.length()) {
        try {
          builder.withSize(
              Long.parseLong(line.substring(SIZE_PREFIX.length())));
        } catch (NumberFormatException e) {
          continue;
        }
      }

      if (builder.isValid()) {
        break;
      }
    }
    return builder;
  }
}
