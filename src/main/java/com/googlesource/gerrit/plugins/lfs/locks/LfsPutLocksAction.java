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

import static com.google.gerrit.extensions.api.lfs.LfsDefinitions.LFS_URL_REGEX_TEMPLATE;
import static com.google.gerrit.extensions.api.lfs.LfsDefinitions.LFS_VERIFICATION_PATH;
import static com.googlesource.gerrit.plugins.lfs.locks.LfsGetLocksAction.LFS_LOCKS_URL_PATTERN;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.lfs.errors.LfsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LfsPutLocksAction extends LfsLocksAction {
  interface Factory extends LfsLocksAction.Factory<LfsPutLocksAction> {}

  private static final Logger log = LoggerFactory.getLogger(LfsPutLocksAction.class);
  private static final Pattern LFS_VERIFICATION_URL_PATTERN =
      Pattern.compile(String.format(LFS_URL_REGEX_TEMPLATE, LFS_VERIFICATION_PATH));

  @Inject
  LfsPutLocksAction(@Assisted LfsLocksContext context) {
    super(context);
  }

  @Override
  protected void doRun() throws LfsException, IOException {
    Matcher matcher = LFS_LOCKS_URL_PATTERN.matcher(context.path);
    if (matcher.matches()) {
      String project = matcher.group(1);
      String lockId = matcher.group(2);
      if (Strings.isNullOrEmpty(lockId)) {
        createLock(project, context);
      } else {
        deleteLock(project, lockId, context);
      }
      return;
    }

    matcher = LFS_VERIFICATION_URL_PATTERN.matcher(context.path);
    if (matcher.matches()) {
      verifyLocks(matcher.group(1), context);
      return;
    }

    throw new LfsException(String.format("Unsupported path %s was provided", context.path));
  }

  private void verifyLocks(String project, LfsLocksContext action) throws IOException {
    log.debug("Verify list of locks for {} project", project);
    //TODO method stub for verifying locks
    action.sendResponse(
        new LfsVerifyLocksResponse(Collections.emptyList(), Collections.emptyList(), null));
  }

  private void deleteLock(String project, String lockId, LfsLocksContext action)
      throws IOException {
    LfsDeleteLockInput input = action.input(LfsDeleteLockInput.class);
    log.debug(
        "Delete (-f {}) lock for {} in project {}",
        Boolean.TRUE.equals(input.force),
        lockId,
        project);
    //TODO: this is just the method stub for lock deletion
    LfsLock lock =
        new LfsLock(
            "random_id",
            "some/path/to/file",
            now(),
            new LfsLockOwner("Lock Owner <lock_owner@example.com>"));
    action.sendResponse(lock);
  }

  private void createLock(String project, LfsLocksContext action) throws IOException {
    LfsCreateLockInput input = action.input(LfsCreateLockInput.class);
    log.debug("Create lock for {} in project {}", input.path, project);
    //TODO: this is just the method stub lock creation
    LfsLock lock =
        new LfsLock(
            "random_id",
            input.path,
            now(),
            new LfsLockOwner("Lock Owner <lock_owner@example.com>"));
    action.sendResponse(lock);
  }
}
