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

import static com.googlesource.gerrit.plugins.lfs.LfsSshRequestAuthorizer.SSH_AUTH_PREFIX;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.gerrit.server.AnonymousUser;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.AccountCache;
import com.google.gerrit.server.account.AccountState;
import com.google.gerrit.server.config.AuthConfig;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
class LfsAuthUserProvider {
  private static final String BASIC_AUTH_PREFIX = "Basic ";

  private final Provider<AnonymousUser> anonymous;
  private final Provider<CurrentUser> user;
  private final AuthConfig authCfg;
  private final LfsSshRequestAuthorizer sshAuth;
  private final AccountCache accounts;
  private final IdentifiedUser.GenericFactory userFactory;

  @Inject
  LfsAuthUserProvider(Provider<AnonymousUser> anonymous,
      Provider<CurrentUser> user,
      AuthConfig authCfg,
      LfsSshRequestAuthorizer sshAuth,
      AccountCache accounts,
      IdentifiedUser.GenericFactory userFactory) {
    this.anonymous = anonymous;
    this.user = user;
    this.authCfg = authCfg;
    this.sshAuth = sshAuth;
    this.accounts = accounts;
    this.userFactory = userFactory;
  }

  CurrentUser getUser(String auth, String project, String operation) {
    if (!Strings.isNullOrEmpty(auth)) {
      if (auth.startsWith(BASIC_AUTH_PREFIX) && authCfg.isGitBasicAuth()) {
        return user.get();
      }

      if (auth.startsWith(SSH_AUTH_PREFIX)) {
        Optional<String> user = sshAuth.getUserFromValidToken(
            auth.substring(SSH_AUTH_PREFIX.length()), project, operation);
        if (user.isPresent()) {
          AccountState acc = accounts.getByUsername(user.get());
          if (acc != null) {
            return userFactory.create(acc);
          }
        }
      }
    }
    return anonymous.get();
  }
}
