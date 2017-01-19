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

  public LfsAuthTokenHandler.Token generateToken(String operation, AnyLongObjectId id,
      int expirationSeconds) {
    return handler.generateToken(expirationSeconds, operation, id.name());
  }

  public boolean verifyAgainstToken(String token, String operation,
      AnyLongObjectId id) {
    return handler.verifyAgainstToken(token, operation, id.name());
  }
}
