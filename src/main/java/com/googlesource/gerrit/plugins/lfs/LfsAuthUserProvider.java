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

import com.google.common.base.Strings;
import com.google.gerrit.server.AnonymousUser;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.AccountCache;
import com.google.gerrit.server.account.AccountState;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;

@Singleton
class LfsAuthUserProvider {
  private static final String BASIC_AUTH_PREFIX = "Basic ";

  private final Provider<AnonymousUser> anonymous;
  private final AccountCache accounts;
  private final IdentifiedUser.GenericFactory userFactory;

  @Inject
  LfsAuthUserProvider(Provider<AnonymousUser> anonymous,
      AccountCache accounts,
      IdentifiedUser.GenericFactory userFactory) {
    this.anonymous = anonymous;
    this.accounts = accounts;
    this.userFactory = userFactory;
  }

  CurrentUser getUser(String auth, Charset cs) {
    if (Strings.isNullOrEmpty(auth)) {
      return anonymous.get();
    }

    if (auth.startsWith(BASIC_AUTH_PREFIX)) {
      String usernamePassword;
      usernamePassword = new String(
          Base64.decodeBase64(auth.substring(BASIC_AUTH_PREFIX.length())), cs);

      String username;
      int splitPos = usernamePassword.indexOf(':');
      if (splitPos < 0) {
        username = usernamePassword;
      } else {
        username = usernamePassword.substring(0, splitPos);
      }

      AccountState acc = accounts.getByUsername(username);
      if (acc != null) {
        return userFactory.create(acc);
      }
    }

    return anonymous.get();
  }
}
