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
import static org.eclipse.jgit.lfs.lib.LongObjectId.zeroId;

import com.googlesource.gerrit.plugins.lfs.LfsAuthTokenHandler;

import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.junit.Test;

public class LfsFsRequestAuthorizerTest {
  private final LfsFsRequestAuthorizer auth =
      new LfsFsRequestAuthorizer(new LfsAuthTokenHandler());

  @Test
  public void testVerifyAgainstToken() throws Exception {
    String token = auth.generateToken("o", zeroId(), 1);
    assertThat(auth.verifyAgainstToken(token, "o", zeroId())).isTrue();
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
}
