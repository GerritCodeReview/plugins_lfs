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

import static com.google.common.truth.Truth.assertThat;

import java.time.Instant;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class LfsDateTimeTest {
  @DataPoints public static String[] timeZones = {"US/Eastern", "Asia/Tokyo", "UTC"};

  @Test
  public void formatWithDefaultTimezone() throws Exception {
    DateTime now = DateTime.now();
    String jodaFormat = ISODateTimeFormat.dateTime().print(now);
    LfsDateTime formatter = LfsDateTime.instance();
    String javaFormat = formatter.format(Instant.ofEpochMilli(now.getMillis()));
    assertThat(javaFormat).isEqualTo(jodaFormat);
  }

  @Theory
  public void formatWithSpecifiedTimezone(String zone) throws Exception {
    DateTime now = DateTime.now().withZone(DateTimeZone.forID(zone));
    String jodaFormat = ISODateTimeFormat.dateTime().withZone(DateTimeZone.forID(zone)).print(now);
    LfsDateTime formatter = LfsDateTime.instance(TimeZone.getTimeZone(zone).toZoneId());
    String javaFormat = formatter.format(Instant.ofEpochMilli(now.getMillis()));
    assertThat(javaFormat).isEqualTo(jodaFormat);
  }
}
