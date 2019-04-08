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

import static org.eclipse.jgit.util.HttpSupport.HDR_AUTHORIZATION;

import java.util.Collections;
import org.eclipse.jgit.lfs.server.Response;

public class ExpiringAction extends Response.Action {
  public final String expiresAt;
  public final Long expiresIn;

  public ExpiringAction(String href, AuthInfo info) {
    this.href = href;
    this.header = Collections.singletonMap(HDR_AUTHORIZATION, info.authToken());
    this.expiresAt = info.expiresAt();
    this.expiresIn = info.expiresIn();
  }
}
