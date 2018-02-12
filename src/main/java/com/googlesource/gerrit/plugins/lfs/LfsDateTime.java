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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LfsDateTime {
  private static final String ISO = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
  private static final String NON_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ";

  private final DateTimeFormatter format;

  private LfsDateTime(boolean strict) {
    format =
        DateTimeFormatter.ofPattern(strict ? ISO : NON_ISO)
            .withZone(ZoneOffset.UTC)
            .withLocale(Locale.getDefault());
  }

  public static class Builder {
    private boolean strict = false;

    public Builder strict() {
      this.strict = true;
      return this;
    }

    public LfsDateTime build() {
      return new LfsDateTime(strict);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public String now() {
    return format.format(Instant.now());
  }

  public String format(Instant instant) {
    return format.format(instant);
  }
}
