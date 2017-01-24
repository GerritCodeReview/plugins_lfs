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

import com.google.common.base.Optional;
import com.google.gerrit.server.CurrentUser;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
class LfsSshRequestAuthorizer {
  static class SshAuthInfo {
    private final LfsAuthTokenHandler.AuthInfo info;

    SshAuthInfo(LfsAuthTokenHandler.AuthInfo info) {
      this.info = info;
    }

    String getAuthToken() {
      return SSH_AUTH_PREFIX + info.authToken;
    }

    DateTime getExpiresAt() {
      return info.expiresAt;
    }
  }

  static final String SSH_AUTH_PREFIX = "Ssh: ";
  private static final int DEFAULT_SSH_TIMEOUT = 10;
  private static final Logger log =
      LoggerFactory.getLogger(LfsSshRequestAuthorizer.class);

  private final LfsAuthTokenHandler auth;
  private final int expirationSeconds;

  @Inject
  LfsSshRequestAuthorizer(LfsAuthTokenHandler auth,
      LfsConfigurationFactory configFactory) {
    this.auth = auth;
    this.expirationSeconds = configFactory.getGlobalConfig().getInt("auth",
        null, "sshExpirationSeconds", DEFAULT_SSH_TIMEOUT);
  }

  SshAuthInfo generateToken(CurrentUser user, String project,
      String operation) {
    Token token = new Token(user, project, operation);
    return new SshAuthInfo(auth.generateToken(expirationSeconds,
        token));
  }

  public Optional<String> verifyToken(String token, String project,
      String operation) {
    Optional<List<String>> values = auth.verifyTokenOnTime(token);
    if (!values.isPresent()) {
      log.error("Token {} expired", token);
      return Optional.absent();
    }

    List<String> list = values.get();
    if (list.size() < 3
        || !project.equals(list.get(1))
        || !operation.equals(list.get(2))) {
      log.error("Invalid data was provided with auth token {}.", token);
      return Optional.absent();
    }

    return Optional.of(list.get(0));
  }

  private final class Token extends LfsAuthTokenHandler.Token {
    private final CurrentUser user;
    private final String project;
    private final String operation;

    Token(CurrentUser user, String project, String operation) {
      this.user = user;
      this.project = project;
      this.operation = operation;
    }

    @Override
    protected String getValue() {
      return new StringBuilder(user.getUserName())
          .append(LfsAuthTokenHandler.Token.DELIMETER).append(project)
          .append(LfsAuthTokenHandler.Token.DELIMETER).append(operation)
          .toString();
    }
  }
}
