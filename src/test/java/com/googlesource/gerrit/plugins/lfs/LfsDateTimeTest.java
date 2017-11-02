
package com.googlesource.gerrit.plugins.lfs;

import static com.google.common.truth.Truth.assertThat;

import java.time.Instant;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

public class LfsDateTimeTest {
  private static final DateTimeFormatter ISO = ISODateTimeFormat.dateTime();

  @Test
  public void format() throws Exception {
    DateTime now = DateTime.now();
    String jodaFormat = ISO.print(now);
    String javaFormat = LfsDateTime.format(Instant.ofEpochMilli(now.getMillis()));
    assertThat(javaFormat).isEqualTo(jodaFormat);
  }
}
