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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.lfs.LfsAuthTokenHandler;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;

@Singleton
public class LfsFsRequestAuthorizer {
  private final LfsAuthTokenHandler handler;

  @Inject
  LfsFsRequestAuthorizer(LfsAuthTokenHandler handler) {
    this.handler = handler;
  }

  public LfsAuthTokenHandler.AuthInfo generateToken(String operation,
      AnyLongObjectId id, int expirationSeconds) {
    return handler.generateToken(expirationSeconds, new Token(operation, id));
  }

  public boolean verifyAgainstToken(String token, String operation,
      AnyLongObjectId id) {
    return handler.verifyAgainstToken(token, new Token(operation, id));
  }

  private static class Token extends LfsAuthTokenHandler.Token {
    private final String operation;
    private final AnyLongObjectId id;

    Token(String operation, AnyLongObjectId id) {
      this.operation = operation;
      this.id = id;
    }

    @Override
    protected String getValue() {
      return new StringBuilder(operation)
          .append(LfsAuthTokenHandler.Token.DELIMETER).append(id.name())
          .toString();
    }
  }
}
