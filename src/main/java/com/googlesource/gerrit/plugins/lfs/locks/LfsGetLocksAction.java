// Copyright (C) 2017 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.lfs.locks;

import static com.googlesource.gerrit.plugins.lfs.LfsPaths.LFS_LOCKS_PATH;
import static com.googlesource.gerrit.plugins.lfs.LfsPaths.URL_REGEX_TEMPLATE;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.lfs.LfsAuthUserProvider;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.lfs.errors.LfsException;
import org.eclipse.jgit.lfs.errors.LfsUnauthorized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LfsGetLocksAction extends LfsLocksAction {
  interface Factory extends LfsLocksAction.Factory<LfsGetLocksAction> {}

  static final Pattern LFS_LOCKS_URL =
      Pattern.compile(String.format(URL_REGEX_TEMPLATE, LFS_LOCKS_PATH));

  private static final Logger log = LoggerFactory.getLogger(LfsGetLocksAction.class);

  @Inject
  LfsGetLocksAction(
      ProjectCache projectCache,
      LfsAuthUserProvider userProvider,
      @Assisted LfsLocksContext context) {
    super(projectCache, userProvider, context);
  }

  @Override
  protected String getProjectName() throws LfsException {
    Matcher matcher = LFS_LOCKS_URL.matcher(context.path);
    if (matcher.matches()) {
      return matcher.group(1);
    }

    throw new LfsException("no repository at " + context.path);
  }

  @Override
  protected void authorizeUser(ProjectControl control) throws LfsUnauthorized {
    if (!control.isReadable()) {
      throwUnauthroizedOp("list locks", control, log);
    }
  }

  @Override
  protected void doRun(ProjectState project, CurrentUser user) throws LfsException, IOException {
    listLocks(project);
  }

  private void listLocks(ProjectState project) throws IOException {
    log.debug("Get list of locks for {} project", project.getProject().getName());
    //TODO method stub for getting project's locks list

    // stub for searching lock by path
    String path = context.getParam("path");
    if (!Strings.isNullOrEmpty(path)) {
      context.sendResponse(
          new LfsGetLocksResponse(
              ImmutableList.<LfsLock>builder()
                  .add(
                      new LfsLock(
                          "random_id",
                          path,
                          now(),
                          new LfsLockOwner("Lock Owner <lock_owner@example.com>")))
                  .build(),
              null));
      return;
    }

    // stub for searching lock by id
    String id = context.getParam("id");
    if (!Strings.isNullOrEmpty(id)) {
      context.sendResponse(
          new LfsGetLocksResponse(
              ImmutableList.<LfsLock>builder()
                  .add(
                      new LfsLock(
                          id,
                          "path/to/file",
                          now(),
                          new LfsLockOwner("Lock Owner <lock_owner@example.com>")))
                  .build(),
              null));
      return;
    }

    // stub for returning all locks
    context.sendResponse(new LfsGetLocksResponse(Collections.emptyList(), null));
  }
}
