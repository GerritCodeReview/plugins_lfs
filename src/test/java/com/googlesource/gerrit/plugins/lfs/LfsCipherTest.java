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

import com.google.common.base.Optional;

import org.junit.Test;

public class LfsCipherTest {
  private final LfsCipher cipher = new LfsCipher();

  @Test
  public void testCipherTextIsDifferentThanInput() throws Exception {
    String plain = "plain text";
    String encoded = cipher.encode(plain);
    assertThat(encoded).isNotEmpty();
    assertThat(encoded).isNotEqualTo(plain);
  }

  @Test
  public void testVerifyDecodeAgainstEncodedInput() throws Exception {
    String plain = "plain text";
    String encoded = cipher.encode(plain);
    Optional<String> decoded = cipher.decode(encoded);
    assertThat(decoded.isPresent()).isTrue();
    assertThat(decoded.get()).isEqualTo(plain);
  }

  @Test
  public void testVerifyDecodeAgainstInvalidInput() throws Exception {
    String plain = "plain text";
    String encoded = cipher.encode(plain);
    // there is a chance that two first chars in token are the same
    // in such case re-generate the token
    while(encoded.charAt(0) == encoded.charAt(1)) {
      encoded = cipher.encode(plain);
    }

    Optional<String> decoded = cipher.decode(encoded.substring(1, 2)
        + encoded.substring(0, 1) + encoded.substring(2));
    assertThat(decoded.isPresent()).isTrue();
    assertThat(decoded.get()).isNotEqualTo(plain);
  }
}
