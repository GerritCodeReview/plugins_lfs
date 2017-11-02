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

package com.googlesource.gerrit.plugins.lfs;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LfsDateTime {
  private final DateTimeFormatter format;

  private LfsDateTime(ZoneId zone) {
    format =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ")
            .withZone(zone)
            .withLocale(Locale.getDefault());
  }

  /* Create an instance with the system default time zone. */
  public static LfsDateTime instance() {
    return new LfsDateTime(ZoneOffset.systemDefault());
  }

  /* Create an instance with the specified time zone. */
  public static LfsDateTime instance(ZoneId zone) {
    return new LfsDateTime(zone);
  }

  public String now() {
    return format.format(Instant.now());
  }

  public String now(int secondsToAdd) {
    return format.format(Instant.now().plusSeconds(secondsToAdd));
  }

  public String format(Instant instant) {
    return format.format(instant);
  }
}
