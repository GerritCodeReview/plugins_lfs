// Copyright (C) 2019 The Android Open Source Project
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

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.server.fs.ObjectUploadListenerWithPostUploadHook;

@Singleton
public class UploadListenerProvider {
  private final Provider<DynamicSet<ObjectUploadListenerWithPostUploadHook.LfsPostUploadHook>>
      hooks;

  @Inject
  UploadListenerProvider(
      Provider<DynamicSet<ObjectUploadListenerWithPostUploadHook.LfsPostUploadHook>> hooks) {
    this.hooks = hooks;
  }

  public ReadListener get(
      NamedFileLfsRepository repository,
      AsyncContext context,
      HttpServletRequest req,
      HttpServletResponse rsp,
      AnyLongObjectId id)
      throws FileNotFoundException, IOException {
    return new ObjectUploadListenerWithPostUploadHook(
        repository, context, req, rsp, id, hooks.get());
  }
}
