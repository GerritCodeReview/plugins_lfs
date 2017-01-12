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

package com.googlesource.gerrit.plugins.lfs.fs;

import static com.google.common.truth.Truth.assertThat;
import static com.googlesource.gerrit.plugins.lfs.fs.LfsFsRequestAuthorizer.DATE_TIME;
import static org.eclipse.jgit.lfs.lib.LongObjectId.zeroId;

import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

public class LfsFsRequestAuthorizerTest {
  private final LfsFsRequestAuthorizer auth = new LfsFsRequestAuthorizer();

  @Test
  public void testExpiredTime() throws Exception {
    DateTime now = DateTime.now().toDateTime(DateTimeZone.UTC);
    // test that even 1s expiration is enough
    assertThat(auth.onTime(DATE_TIME.print(now.minusSeconds(1)), "o", "id"))
            .isFalse();
  }

  @Test
  public void testOnTime() throws Exception {
    DateTime now = DateTime.now().toDateTime(DateTimeZone.UTC);
    // if there is at least 1s before there is no timeout
    assertThat(auth.onTime(DATE_TIME.print(now.plusSeconds(1)), "o", "id"))
            .isTrue();
  }

  @Test
  public void testVerifyAgainstMissingToken() throws Exception {
    assertThat(auth.verifyAgainstToken("", "o", zeroId())).isFalse();
    assertThat(auth.verifyAgainstToken(null, "o", zeroId())).isFalse();
  }

  @Test
  public void testVerifyAgainstToken() throws Exception {
    String token = auth.generateToken("o", zeroId(), 1);
    assertThat(auth.verifyAgainstToken(token, "o", zeroId())).isTrue();
  }

  @Test
  public void testVerifyAgainstInvalidToken() throws Exception {
    String token = auth.generateToken("o", zeroId(), 1);
    // replace 1st and 2nd token letters with each other
    assertThat(auth.verifyAgainstToken(
        token.substring(1, 2) + token.substring(0, 1) + token.substring(2), "o",
        zeroId())).isFalse();
  }

  @Test
  public void testVerifyAgainstDifferentOperation() throws Exception {
    String token = auth.generateToken("o", zeroId(), 1);
    assertThat(auth.verifyAgainstToken(token, "p", zeroId())).isFalse();
  }

  @Test
  public void testVerifyAgainstDifferentObjectId() throws Exception {
    String token = auth.generateToken("o", zeroId(), 1);
    assertThat(auth.verifyAgainstToken(token, "o",
        LongObjectId.fromString(
            "123456789012345678901234567890"
            + "123456789012345678901234567890"
            + "1234"))).isFalse();
  }

  @Test
  public void testVerifyAgainstExpiredToken() throws Exception {
    // generate already expired token
    String token = auth.generateToken("o", zeroId(), -1);
    assertThat(auth.verifyAgainstToken(token, "o", zeroId())).isFalse();
  }
}
