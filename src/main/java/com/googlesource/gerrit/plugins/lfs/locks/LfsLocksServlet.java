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

package com.googlesource.gerrit.plugins.lfs.locks;

import static com.googlesource.gerrit.plugins.lfs.LfsPaths.LFS_LOCKS_PATH_REGEX;
import static com.googlesource.gerrit.plugins.lfs.LfsPaths.LFS_VERIFICATION_PATH;
import static com.googlesource.gerrit.plugins.lfs.LfsPaths.URL_REGEX_TEMPLATE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class LfsLocksServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public static final String LFS_LOCKS_REST =
      String.format(URL_REGEX_TEMPLATE, LFS_LOCKS_PATH_REGEX + "|" + LFS_VERIFICATION_PATH);

  private final LfsGetLocksAction.Factory getters;
  private final LfsPutLocksAction.Factory putters;

  @Inject
  LfsLocksServlet(LfsGetLocksAction.Factory getters, LfsPutLocksAction.Factory putters) {
    this.getters = getters;
    this.putters = putters;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    LfsLocksContext context = new LfsLocksContext(req, resp);
    getters.create(context).run();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    LfsLocksContext context = new LfsLocksContext(req, resp);
    putters.create(context).run();
  }
}
