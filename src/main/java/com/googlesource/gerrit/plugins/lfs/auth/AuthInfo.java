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

import com.googlesource.gerrit.plugins.lfs.LfsDateTime;
import java.time.Instant;

public class AuthInfo {
  private final String authToken;
  private final Instant issued;
  private final Long expiresIn;

  /**
   * @param authToken token
   * @param issued time at which the token was issued
   * @param expiresIn expiry duration in seconds
   */
  public AuthInfo(String authToken, Instant issued, Long expiresIn) {
    this.authToken = authToken;
    this.issued = issued;
    this.expiresIn = expiresIn;
  }

  /**
   * Get the auth token
   *
   * @return the auth token
   */
  public String authToken() {
    return authToken;
  }

  /**
   * Get a formatted timestamp of the time at which this authentication expires
   *
   * @return timestamp
   */
  public String expiresAt() {
    return LfsDateTime.format(issued.plusSeconds(expiresIn));
  }

  /**
   * Get the time duration after which this authentication expires
   *
   * @return the time duration in seconds
   */
  public Long expiresIn() {
    return expiresIn;
  }
}
