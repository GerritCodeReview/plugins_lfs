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
import static com.googlesource.gerrit.plugins.lfs.fs.LfsFsRequestAuthorizer.TOKEN_TIMEOUT;
import static org.eclipse.jgit.lfs.lib.LongObjectId.zeroId;

import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

public class LfsFsRequestAuthorizerTest {
  private LfsFsRequestAuthorizer objectUnderTest;

  @Before
  public void setUp() throws Exception {
    objectUnderTest = new LfsFsRequestAuthorizer();
  }

  @Test
  public void testOnTime() throws Exception {
    DateTime now = DateTime.now().toDateTime(DateTimeZone.UTC);
    // test that even 1s expiration is enough
    assertThat(
        objectUnderTest.onTime(DATE_TIME.print(now.minusSeconds(1)), "o", "id"))
            .isFalse();

    // if there is at least 1s before there is no timeout
    assertThat(
        objectUnderTest.onTime(DATE_TIME.print(now.plusSeconds(2)), "o", "id"))
            .isTrue();
  }

  @Test
  public void testVerifyAgainstToken() throws Exception {
    // edge cases when token is not provided
    assertThat(objectUnderTest.verifyAgainstToken("", "o", zeroId())).isFalse();
    assertThat(objectUnderTest.verifyAgainstToken(null, "o", zeroId()))
        .isFalse();

    // test with token
    String token = objectUnderTest.generateToken("o", zeroId());
    assertThat(objectUnderTest.verifyAgainstToken(token, "o", zeroId()))
        .isTrue();
    // replace 1st and 2nd token letters with each other
    assertThat(objectUnderTest.verifyAgainstToken(
        token.substring(1, 2) + token.substring(0, 1) + token.substring(2), "o",
        zeroId())).isFalse();
    // change operation
    assertThat(objectUnderTest.verifyAgainstToken(token, "p", zeroId()))
        .isFalse();
    // change object id
    assertThat(objectUnderTest.verifyAgainstToken(token, "o",
        LongObjectId.fromString(
            "123456789012345678901234567890"
            + "123456789012345678901234567890"
            + "1234"))).isFalse();

    // test token timeout
    Thread.sleep((TOKEN_TIMEOUT + 1) * 1000);
    assertThat(objectUnderTest.verifyAgainstToken(token, "o", zeroId()))
        .isFalse();
  }
}
