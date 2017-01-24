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
    String encrypted = cipher.encrypt(plain);
    assertThat(encrypted).isNotEmpty();
    assertThat(encrypted).isNotEqualTo(plain);
  }

  @Test
  public void testVerifyDecodeAgainstEncodedInput() throws Exception {
    String plain = "plain text";
    String encrypted = cipher.encrypt(plain);
    Optional<String> decrypted = cipher.decrypt(encrypted);
    assertThat(decrypted.isPresent()).isTrue();
    assertThat(decrypted.get()).isEqualTo(plain);
  }

  @Test
  public void testVerifyDecodeAgainstInvalidInput() throws Exception {
    String plain = "plain text";
    String encrypted = cipher.encrypt(plain);
    // there is a chance that two first chars in token are the same
    // in such case re-generate the token
    while(encrypted.charAt(0) == encrypted.charAt(1)) {
      encrypted = cipher.encrypt(plain);
    }

    Optional<String> decrypted = cipher.decrypt(encrypted.substring(1, 2)
        + encrypted.substring(0, 1) + encrypted.substring(2));
    assertThat(decrypted.isPresent()).isTrue();
    assertThat(decrypted.get()).isNotEqualTo(plain);
  }
}
