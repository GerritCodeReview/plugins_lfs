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

import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.sshd.BaseCommand.Failure;
import com.google.gerrit.sshd.BaseCommand.UnloggedFailure;
import com.google.gerrit.sshd.plugin.LfsPluginAuthCommand;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.lfs.server.Response;
import org.eclipse.jgit.lib.Config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@Singleton
public class LfsSshAuth implements LfsPluginAuthCommand.LfsSshPluginAuth {
  private final String canonicalWebUrl;
  private final Gson gson;

  @Inject
  LfsSshAuth(@GerritServerConfig Config config) {
    this.canonicalWebUrl = config.getString("gerrit", null, "canonicalWebUrl");
    this.gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .disableHtmlEscaping()
        .create();
  }

  @Override
  public String authenticate(CurrentUser user, List<String> args)
      throws UnloggedFailure, Failure, Exception {
    try {
      URL url = new URL(canonicalWebUrl);
      String href = url.getProtocol() + "://" + url.getAuthority()
          + url.getPath() + "/" + args.get(0) + "/info/lfs";
      Response.Action response = new Response.Action();
      response.href = href;
      response.header =
          Collections.singletonMap("Authorization", "not:required");

      return gson.toJson(response);

    } catch (MalformedURLException e) {
      throw new Failure(1, "Server configuration error: "
          + "forming Git LFS endpoint URL from canonicalWebUrl ["
          + canonicalWebUrl + "] failed.");
    }
  }
}
