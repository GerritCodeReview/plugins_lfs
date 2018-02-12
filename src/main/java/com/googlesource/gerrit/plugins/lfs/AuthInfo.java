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

import java.time.Instant;

public class AuthInfo {
  private static final LfsDateTime DATETIME = LfsDateTime.builder().strict().build();
  private final String authToken;
  private final Instant issued;
  private final Integer expiresIn;

  /**
   * @param authToken token
   * @param issued issued time at which the token was issued
   * @param expiresIn expiry time in seconds
   */
  public AuthInfo(String authToken, Instant issued, Integer expiresIn) {
    this.authToken = authToken;
    this.issued = issued;
    this.expiresIn = expiresIn;
  }

  public String authToken() {
    return authToken;
  }

  public String expiresAt() {
    return DATETIME.format(issued.plusSeconds(expiresIn));
  }

  /** @return the expiry duration in milliseconds. */
  public Integer expiresIn() {
    return expiresIn * 1000;
  }
}
