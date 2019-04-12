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

import com.google.common.flogger.FluentLogger;
import com.google.gerrit.server.CurrentUser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
class LfsSshRequestAuthorizer {
  static class SshAuthInfo extends AuthInfo {
    SshAuthInfo(String authToken, Instant issued, Long expiresIn) {
      super(SSH_AUTH_PREFIX + authToken, issued, expiresIn);
    }
  }

  private static final FluentLogger log = FluentLogger.forEnclosingClass();
  private static final int DEFAULT_SSH_TIMEOUT = 10;
  static final String SSH_AUTH_PREFIX = "Ssh: ";
  private final Processor processor;
  private final Long expiresIn;

  @Inject
  LfsSshRequestAuthorizer(Processor processor, LfsConfigurationFactory configFactory) {
    this.processor = processor;
    long timeout = DEFAULT_SSH_TIMEOUT;
    try {
      timeout =
          configFactory
              .getGlobalConfig()
              .getInt("auth", null, "sshExpirationSeconds", DEFAULT_SSH_TIMEOUT);
    } catch (IllegalArgumentException e) {
      log.atWarning().withCause(e).log(
          "Reading expiration timeout failed with error. Falling back to default %d",
          DEFAULT_SSH_TIMEOUT);
    }
    this.expiresIn = timeout;
  }

  SshAuthInfo generateAuthInfo(CurrentUser user, String project, String operation) {
    LfsSshAuthToken token =
        new LfsSshAuthToken(user.getUserName().get(), project, operation, Instant.now(), expiresIn);
    return new SshAuthInfo(processor.serialize(token), token.issued, token.expiresIn);
  }

  Optional<String> getUserFromValidToken(String authToken, String project, String operation) {
    Optional<LfsSshAuthToken> token = processor.deserialize(authToken);
    if (!token.isPresent()) {
      return Optional.empty();
    }
    Verifier verifier = new Verifier(token.get(), project, operation);
    if (!verifier.verify()) {
      log.atSevere().log("Invalid data was provided with auth token %s.", authToken);
      return Optional.empty();
    }
    return Optional.of(token.get().user);
  }

  static class Processor extends LfsAuthToken.Processor<LfsSshAuthToken> {
    @Inject
    protected Processor(LfsCipher cipher) {
      super(cipher);
    }

    @Override
    protected List<String> getValues(LfsSshAuthToken token) {
      List<String> values = new ArrayList<>(4);
      values.add(token.user);
      values.add(token.project);
      values.add(token.operation);
      values.add(LfsDateTime.format(token.issued));
      values.add(String.valueOf(token.expiresIn));
      return values;
    }

    @Override
    protected Optional<LfsSshAuthToken> createToken(List<String> values) {
      if (values.size() != 5) {
        return Optional.empty();
      }
      return Optional.of(
          new LfsSshAuthToken(
              values.get(0),
              values.get(1),
              values.get(2),
              values.get(3),
              Long.valueOf(values.get(4))));
    }
  }

  private static class Verifier extends LfsAuthToken.Verifier<LfsSshAuthToken> {
    private final String project;
    private final String operation;

    protected Verifier(LfsSshAuthToken token, String project, String operation) {
      super(token);
      this.project = project;
      this.operation = operation;
    }

    @Override
    protected boolean verifyTokenValues() {
      return project.equals(token.project) && operation.equals(token.operation);
    }
  }

  private static class LfsSshAuthToken extends LfsAuthToken {
    private final String user;
    private final String project;
    private final String operation;

    LfsSshAuthToken(String user, String project, String operation, Instant issued, Long expiresIn) {
      super(issued, expiresIn);
      this.user = user;
      this.project = project;
      this.operation = operation;
    }

    LfsSshAuthToken(String user, String project, String operation, String issued, Long expiresIn) {
      super(issued, expiresIn);
      this.user = user;
      this.project = project;
      this.operation = operation;
    }
  }
}
