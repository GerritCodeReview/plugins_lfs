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

import static com.google.gerrit.httpd.plugins.LfsPluginServlet.URL_REGEX;
import static com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository.CONTENT_PATH;

import com.google.gerrit.httpd.plugins.HttpPluginModule;

import com.googlesource.gerrit.plugins.lfs.fs.LfsFsContentServlet;
import com.googlesource.gerrit.plugins.lfs.fs.LocalLfsTransferDescriptor;

public class HttpModule extends HttpPluginModule {
  @Override
  protected void configureServlets() {
    serveRegex(URL_REGEX).with(LfsApiServlet.class);
    bind(LocalLfsTransferDescriptor.class);
    serve("/" + CONTENT_PATH + "/*").with(LfsFsContentServlet.class);
  }
}
