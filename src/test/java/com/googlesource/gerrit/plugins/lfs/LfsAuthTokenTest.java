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
import static com.googlesource.gerrit.plugins.lfs.LfsAuthToken.ISO;
import static com.googlesource.gerrit.plugins.lfs.LfsAuthToken.Verifier.onTime;

import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LfsAuthTokenTest {
  private final LfsCipher cipher = new LfsCipher();

  @Test
  public void testExpiredTime() throws Exception {
    DateTime now = now();
    // test that even 1ms expiration is enough
    assertThat(onTime(ISO.print(now.minusMillis(1)))).isFalse();
  }

  @Test
  public void testOnTime() throws Exception {
    DateTime now = now();
    // if there is at least 1ms before there is no timeout
    assertThat(onTime(ISO.print(now.plusMillis(1)))).isTrue();
  }

  @Test
  public void testTokenSerializationDeserialization() throws Exception {
    TestTokenProessor processor = new TestTokenProessor(cipher);
    TestToken token = new TestToken(0);
    String serialized = processor.serialize(token);

    assertThat(serialized).isNotEmpty();

    Optional<TestToken> deserialized = processor.deserialize(serialized);
    assertThat(deserialized.isPresent()).isTrue();
    assertThat(token.expiresAt).isEqualTo(deserialized.get().expiresAt);
  }

  @Test
  public void testTokenOnTime() throws Exception {
    TestToken token = new TestToken(1);
    TestTokenVerifier verifier = new TestTokenVerifier(token);
    assertThat(verifier.verify()).isTrue();
  }

  @Test
  public void testTokenExpired() throws Exception {
    TestToken token = new TestToken(-1);
    TestTokenVerifier verifier = new TestTokenVerifier(token);
    assertThat(verifier.verify()).isFalse();
  }

  private DateTime now() {
    return DateTime.now().toDateTime(DateTimeZone.UTC);
  }

  private class TestToken extends LfsAuthToken {
    TestToken(int expirationSeconds) {
      super(expirationSeconds);
    }

    TestToken(String expiresAt) {
      super(expiresAt);
    }
  }

  private class TestTokenProessor extends LfsAuthToken.Processor<TestToken> {
    TestTokenProessor(LfsCipher cipher) {
      super(cipher);
    }

    @Override
    protected List<String> getValues(TestToken token) {
      List<String> values = new ArrayList<>(2);
      values.add(token.expiresAt);
      return values;
    }

    @Override
    protected Optional<TestToken> createToken(List<String> values) {
      return Optional.of(new TestToken(values.get(0)));
    }
  }

  private class TestTokenVerifier extends LfsAuthToken.Verifier<TestToken> {
    protected TestTokenVerifier(TestToken token) {
      super(token);
    }

    @Override
    protected boolean verifyTokenValues() {
      return true;
    }
  }
}
