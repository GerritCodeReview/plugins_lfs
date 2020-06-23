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

import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.gerrit.sshd.BaseCommand.Failure;
import com.google.gerrit.sshd.BaseCommand.UnloggedFailure;
import com.google.gerrit.sshd.plugin.LfsPluginAuthCommand;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.lfs.LfsGson;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Singleton
public class LfsSshAuth implements LfsPluginAuthCommand.LfsSshPluginAuth {
  private final LfsSshRequestAuthorizer auth;
  private final String canonicalWebUrl;
  private final LfsGson gson;

  @Inject
  LfsSshAuth(
      LfsSshRequestAuthorizer auth,
      @CanonicalWebUrl Provider<String> canonicalWebUrl,
      LfsGson gson) {
    this.auth = auth;
    this.canonicalWebUrl = canonicalWebUrl.get();
    this.gson = gson;
  }

  @Override
  public String authenticate(CurrentUser user, List<String> args) throws Failure {
    if (args.size() != 2) {
      throw new UnloggedFailure(1, "Unexpected number of arguments");
    }
    try {
      URL url = new URL(canonicalWebUrl);
      String path = url.getPath();
      String project = args.get(0);
      String operation = args.get(1);
      StringBuilder href =
          new StringBuilder(url.getProtocol())
              .append("://")
              .append(url.getAuthority())
              .append(path)
              .append(path.endsWith("/") ? "" : "/")
              .append(project)
              .append("/info/lfs");
      LfsSshRequestAuthorizer.SshAuthInfo info = auth.generateAuthInfo(user, project, operation);
      ExpiringAction action = new ExpiringAction(href.toString(), info);
      return gson.toJson(action);
    } catch (MalformedURLException e) {
      throw new Failure(
          1,
          "Server configuration error: "
              + "forming Git LFS endpoint URL from canonicalWebUrl ["
              + canonicalWebUrl
              + "] failed.");
    }
  }
}
