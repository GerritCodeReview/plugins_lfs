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

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

import java.io.IOException;
import org.eclipse.jgit.lfs.errors.LfsException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

abstract class LfsLocksAction {
  interface Factory<T extends LfsLocksAction> {
    T create(LfsLocksContext context);
  }

  private static final DateTimeFormatter ISO = ISODateTimeFormat.dateTime();

  protected final LfsLocksContext context;

  protected LfsLocksAction(LfsLocksContext context) {
    this.context = context;
  }

  public void run() throws IOException {
    try {
      doRun();
    } catch (LfsException e) {
      context.sendError(SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  protected abstract void doRun() throws LfsException, IOException;

  protected String now() {
    return ISO.print(DateTime.now().toDateTime(DateTimeZone.UTC));
  }
}
