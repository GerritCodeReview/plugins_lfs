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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.googlesource.gerrit.plugins.lfs.auth.LfsSshRequestAuthorizer.SSH_AUTH_PREFIX;

import com.google.common.base.Strings;
import com.google.gerrit.server.AnonymousUser;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.AccountCache;
import com.google.gerrit.server.account.AccountState;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.Optional;

@Singleton
public class LfsAuthUserProvider {
  private final Provider<AnonymousUser> anonymous;
  private final Provider<CurrentUser> currentUser;
  private final LfsSshRequestAuthorizer sshAuth;
  private final AccountCache accounts;
  private final IdentifiedUser.GenericFactory userFactory;

  @Inject
  LfsAuthUserProvider(
      Provider<AnonymousUser> anonymous,
      Provider<CurrentUser> currentUser,
      LfsSshRequestAuthorizer sshAuth,
      AccountCache accounts,
      IdentifiedUser.GenericFactory userFactory) {
    this.anonymous = anonymous;
    this.currentUser = currentUser;
    this.sshAuth = sshAuth;
    this.accounts = accounts;
    this.userFactory = userFactory;
  }

  public CurrentUser getUser(String auth, String project, String operation) {
    if (!Strings.isNullOrEmpty(auth) && auth.startsWith(SSH_AUTH_PREFIX)) {
      Optional<String> user =
          sshAuth.getUserFromValidToken(
              auth.substring(SSH_AUTH_PREFIX.length()), project, operation);
      if (user.isPresent()) {
        Optional<AccountState> acc = accounts.getByUsername(user.get());
        if (acc.isPresent()) {
          return userFactory.create(acc.get());
        }
      }
    }
    return firstNonNull(currentUser.get(), anonymous.get());
  }
}
