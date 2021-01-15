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

package com.googlesource.gerrit.plugins.lfs.auth;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static java.time.temporal.ChronoUnit.MILLIS;

import com.googlesource.gerrit.plugins.lfs.LfsDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class LfsAuthTokenTest {
  private final LfsCipher cipher = new LfsCipher();

  @Test
  public void testTokenSerializationDeserialization() throws Exception {
    TestTokenProessor processor = new TestTokenProessor(cipher);
    TestToken token = new TestToken(Instant.now().truncatedTo(MILLIS), 0L);
    String serialized = processor.serialize(token);
    assertThat(serialized).isNotEmpty();
    Optional<TestToken> deserialized = processor.deserialize(serialized);
    assertThat(deserialized).isPresent();
    assertThat(token.issued).isEqualTo(deserialized.get().issued);
  }

  @Test
  public void testTokenOnTime() throws Exception {
    Instant when = Instant.now();
    TestToken token = new TestToken(when, 1L);
    TestTokenVerifier verifier = new TestTokenVerifier(token);
    assertThat(verifier.onTime(when.plusMillis(999))).isTrue();
  }

  @Test
  public void testTokenExpired() throws Exception {
    Instant when = Instant.now();
    TestToken token = new TestToken(when, 1L);
    TestTokenVerifier verifier = new TestTokenVerifier(token);
    assertThat(verifier.onTime(when.plusMillis(1001))).isFalse();
  }

  private class TestToken extends LfsAuthToken {
    TestToken(Instant now, Long expiresIn) {
      super(now, expiresIn);
    }

    TestToken(String expiresAt, Long expiresIn) {
      super(expiresAt, expiresIn);
    }
  }

  private class TestTokenProessor extends LfsAuthToken.Processor<TestToken> {
    TestTokenProessor(LfsCipher cipher) {
      super(cipher);
    }

    @Override
    protected List<String> getValues(TestToken token) {
      List<String> values = new ArrayList<>(2);
      values.add(LfsDateTime.format(token.issued));
      values.add(String.valueOf(token.expiresIn));
      return values;
    }

    @Override
    protected Optional<TestToken> createToken(List<String> values) {
      return Optional.of(new TestToken(values.get(0), Long.valueOf(values.get(1))));
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
