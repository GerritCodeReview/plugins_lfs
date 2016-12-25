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

package com.googlesource.gerrit.plugins.lfs;

import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.sshd.BaseCommand.UnloggedFailure;
import com.google.gerrit.sshd.CommandMetaData;
import com.google.gerrit.sshd.SshCommand;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.net.MalformedURLException;
import java.net.URL;

import org.kohsuke.args4j.Argument;

import org.eclipse.jgit.lib.Config;

@CommandMetaData(name = "git-lfs-authenticate",
                 description = "Provides HTTP/HTTPS URL for logged in SSH-user")
public final class LfsSshGitAuthenticateCommand extends SshCommand {

  private final String username;
  private final String canonicalWebUrl;

  @Inject
  LfsSshGitAuthenticateCommand(final Provider<CurrentUser> userProvider,
                               final @GerritServerConfig Config config) {
    this.username = userProvider.get().getUserName();
    this.canonicalWebUrl = config.getString("gerrit", null, "canonicalWebUrl");
  }

  @Argument(index = 0, required = true,
            metaVar = "PATH", usage = "SSH repo path")
  private String sshRepoPath;

  @Argument(index = 1, required = true,
            metaVar = "DIR", usage = "Direction of the transfer upload/download")
  private String operation;

  @Argument(index = 2, required = false,
            metaVar = "OID", usage = "The binary object for this authention request (OID)")
  private String oid;

  public class AuthResponse {
    public String href;
    public Header header;

    public class Header {
      String Authorization;
    }

    AuthResponse(String href) {
      this.href = href;
      this.header = new Header();
      // Authorization is not used yet
      this.header.Authorization = "not:required";
    }
  }

  @Override
  public void run() throws UnloggedFailure {

    // The git-lfs client expects this structure for the git-lfs-authenticate API
    //  {
    //    "href": "https://lfs-server.com/foo/bar",
    //    "header": {
    //      "Authorization": "RemoteAuth some-token"
    //    },
    //    "expires_at": "2016-11-10T15:29:07Z"
    //  }

    if (canonicalWebUrl == null) {
      throw new UnloggedFailure(22,
                "Server configuration error: " +
                "gerrit.canonicalWebUrl not configured");
    }

    try {
      final URL url = new URL(canonicalWebUrl);
      final String href = url.getProtocol() + "://" + username + "@"
          + url.getAuthority() + "/" + sshRepoPath + "/info/lfs";

      stdout.print(new Gson().toJson(new AuthResponse(href)));

    } catch (MalformedURLException e) {
      throw new UnloggedFailure(23,
                "Server configuration error: " +
                "gerrit.canonicalWebUrl is invalid:" + canonicalWebUrl);
    }
  }
}
