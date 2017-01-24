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
import static com.googlesource.gerrit.plugins.lfs.LfsAuthTokenHandler.DATE_TIME;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

public class LfsAuthTokenHandlerTest {
  private final LfsAuthTokenHandler auth = new LfsAuthTokenHandler();
  private final LfsAuthTokenHandler.Token token =
      new LfsAuthTokenHandler.Token() {
        @Override
        protected String getValue() {
          return "";
        }
      };

  @Test
  public void testExpiredTime() throws Exception {
    DateTime now = nowRoundedTo1s();
    // test that even 1ms expiration is enough
    assertThat(auth.onTime(DATE_TIME.print(now.minusMillis(1)), "s"))
        .isFalse();
  }

  @Test
  public void testOnTime() throws Exception {
    DateTime now = nowRoundedTo1s();
    // if there is at least 1ms before there is no timeout
    assertThat(auth.onTime(DATE_TIME.print(now.plusMillis(1)), "s")).isTrue();
  }

  @Test
  public void testVerifyAgainstMissingToken() throws Exception {
    assertThat(auth.verifyAgainstToken("", token)).isFalse();
    assertThat(auth.verifyAgainstToken(null, token)).isFalse();
  }

  @Test
  public void testVerifyAgainstToken() throws Exception {
    LfsAuthTokenHandler.AuthInfo info = auth.generateAuthInfo(1, token);
    assertThat(auth.verifyAgainstToken(info.authToken, token)).isTrue();
  }

  @Test
  public void testVerifyAgainstInvalidToken() throws Exception {
    LfsAuthTokenHandler.AuthInfo info = auth.generateAuthInfo(1, token);
    String authToken = info.authToken;

    // there is a chance that two first chars in token are the same
    // in such case re-generate the token
    while(authToken.charAt(0) == authToken.charAt(1)) {
      info = auth.generateAuthInfo(1, token);
      authToken = info.authToken;
    }

    // replace 1st and 2nd token letters with each other
    assertThat(auth.verifyAgainstToken(authToken.substring(1, 2)
        + authToken.substring(0, 1) + authToken.substring(2), token)).isFalse();
  }

  @Test
  public void testVerifyAgainstExpiredToken() throws Exception {
    // generate already expired authInfo
    LfsAuthTokenHandler.AuthInfo info = auth.generateAuthInfo(-1, token);
    assertThat(auth.verifyAgainstToken(info.authToken, token)).isFalse();
  }

  private DateTime nowRoundedTo1s() {
    long millis = DateTime.now().toDateTime(DateTimeZone.UTC).getMillis();
    DateTime now = new DateTime(millis - (millis % 1000), DateTimeZone.UTC);
    return now;
  }
}
