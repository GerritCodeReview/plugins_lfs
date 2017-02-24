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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
class LfsSshRequestAuthorizer {
  static class SshAuthInfo extends AuthInfo {
    SshAuthInfo(String authToken, String expiresAt) {
      super(SSH_AUTH_PREFIX + authToken, expiresAt);
    }
  }

  private static final Logger log =
      LoggerFactory.getLogger(LfsSshRequestAuthorizer.class);
  private static final int DEFAULT_SSH_TIMEOUT = 10;
  static final String SSH_AUTH_PREFIX = "Ssh: ";

  private final Processor processor;
  private final int expirationSeconds;

  @Inject
  LfsSshRequestAuthorizer(Processor processor,
      LfsConfigurationFactory configFactory) {
    this.processor = processor;
    int timeout = DEFAULT_SSH_TIMEOUT;
    try {
      timeout = configFactory.getGlobalConfig().getInt("auth",
        null, "sshExpirationSeconds", DEFAULT_SSH_TIMEOUT);
    } catch (IllegalArgumentException e) {
      log.warn("Reading expiration timeout failed with error."
          + " Falling back to default {}", DEFAULT_SSH_TIMEOUT, e);
    }
    this.expirationSeconds = timeout;
  }

  SshAuthInfo generateAuthInfo(CurrentUser user, String project,
      String operation) {
    LfsSshAuthToken token = new LfsSshAuthToken(user.getUserName(), project,
        operation, expirationSeconds);
    return new SshAuthInfo(processor.serialize(token), token.expiresAt);
  }

  Optional<String> getUserFromValidToken(String authToken,
      String project, String operation) {
    Optional<LfsSshAuthToken> token = processor.deserialize(authToken);
    if (!token.isPresent()) {
      return Optional.absent();
    }

    Verifier verifier = new Verifier(token.get(), project, operation);
    if (!verifier.verify()) {
      log.error("Invalid data was provided with auth token {}.", authToken);
      return Optional.absent();
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
      values.add(token.expiresAt);
      return values;
    }

    @Override
    protected Optional<LfsSshAuthToken> createToken(List<String> values) {
      if (values.size() != 4) {
        return Optional.absent();
      }

      return Optional.of(new LfsSshAuthToken(values.get(0), values.get(1),
          values.get(2), values.get(3)));
    }
  }

  private static class Verifier extends LfsAuthToken.Verifier<LfsSshAuthToken> {
    private final String project;
    private final String operation;

    protected Verifier(LfsSshAuthToken token, String project,
        String operation) {
      super(token);
      this.project = project;
      this.operation = operation;
    }

    @Override
    protected boolean verifyTokenValues() {
      return project.equals(token.project)
          && operation.equals(token.operation);
    }
  }

  private static class LfsSshAuthToken extends LfsAuthToken {
    private final String user;
    private final String project;
    private final String operation;

    LfsSshAuthToken(String user, String project, String operation,
        int expirationSeconds) {
      super(expirationSeconds);
      this.user = user;
      this.project = project;
      this.operation = operation;
    }

    LfsSshAuthToken(String user, String project, String operation,
        String expiresAt) {
      super(expiresAt);
      this.user = user;
      this.project = project;
      this.operation = operation;
    }
  }
}
