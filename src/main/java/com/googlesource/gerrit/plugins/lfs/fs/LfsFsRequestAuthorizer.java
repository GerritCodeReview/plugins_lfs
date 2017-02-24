// Copyright (C) 2016 The Android Open Source Project
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

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.AuthInfo;
import com.googlesource.gerrit.plugins.lfs.LfsAuthToken;
import com.googlesource.gerrit.plugins.lfs.LfsCipher;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.LongObjectId;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class LfsFsRequestAuthorizer {
  private final Processor processor;

  @Inject
  LfsFsRequestAuthorizer(Processor processor) {
    this.processor = processor;
  }

  public AuthInfo generateAuthInfo(String operation, AnyLongObjectId id,
      int expirationSeconds) {
    LfsFsAuthToken token = new LfsFsAuthToken(operation, id, expirationSeconds);
    return new AuthInfo(processor.serialize(token), token.expiresAt);
  }

  public boolean verifyAuthInfo(String authToken, String operation,
      AnyLongObjectId id) {
    Optional<LfsFsAuthToken> token = processor.deserialize(authToken);
    if (!token.isPresent()) {
      return false;
    }

    return new Verifier(token.get(), operation, id).verify();
  }

  static class Processor extends LfsAuthToken.Processor<LfsFsAuthToken> {
    @Inject
    protected Processor(LfsCipher cipher) {
      super(cipher);
    }

    @Override
    protected List<String> getValues(LfsFsAuthToken token) {
      List<String> values = new ArrayList<>(3);
      values.add(token.operation);
      values.add(token.id.getName());
      values.add(token.expiresAt);
      return values;
    }

    @Override
    protected Optional<LfsFsAuthToken> createToken(List<String> values) {
      if (values.size() != 3) {
        return Optional.absent();
      }

      return Optional.of(new LfsFsAuthToken(values.get(0),
          LongObjectId.fromString(values.get(1)), values.get(2)));
    }
  }

  private static class Verifier extends LfsAuthToken.Verifier<LfsFsAuthToken> {
    private final String operation;
    private final AnyLongObjectId id;

    protected Verifier(LfsFsAuthToken token,
        String operation, AnyLongObjectId id) {
      super(token);
      this.operation = operation;
      this.id = id;
    }

    @Override
    protected boolean verifyTokenValues() {
      return operation.equals(token.operation)
          && id.getName().equals(token.id.getName());
    }
  }

  private static class LfsFsAuthToken extends LfsAuthToken {
    private final String operation;
    private final AnyLongObjectId id;

    LfsFsAuthToken(String operation, AnyLongObjectId id,
        int expirationSeconds) {
      super(expirationSeconds);
      this.operation = operation;
      this.id = id;
    }

    LfsFsAuthToken(String operation, AnyLongObjectId id, String expiresAt) {
      super(expiresAt);
      this.operation = operation;
      this.id = id;
    }
  }
}
