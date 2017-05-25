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

import static com.google.gerrit.extensions.api.lfs.LfsDefinitions.LFS_LOCKS_PATH_REGEX;
import static com.google.gerrit.extensions.api.lfs.LfsDefinitions.LFS_URL_REGEX_TEMPLATE;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.lfs.errors.LfsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LfsGetLocksAction extends LfsLocksAction {
  interface Factory extends LfsLocksAction.Factory<LfsGetLocksAction> {}

  static final Pattern LFS_LOCKS_URL_PATTERN =
      Pattern.compile(String.format(LFS_URL_REGEX_TEMPLATE, LFS_LOCKS_PATH_REGEX));

  private static final Logger log = LoggerFactory.getLogger(LfsGetLocksAction.class);

  @Inject
  LfsGetLocksAction(@Assisted LfsLocksContext context) {
    super(context);
  }

  @Override
  protected void doRun() throws LfsException, IOException {
    Matcher matcher = LFS_LOCKS_URL_PATTERN.matcher(context.path);
    if (matcher.matches()) {
      String project = matcher.group(1);
      listLocks(project);
    }

    throw new LfsException("no repository at " + context.path);
  }

  private void listLocks(String project) throws IOException {
    log.debug("Get list of locks for {} project", project);
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
