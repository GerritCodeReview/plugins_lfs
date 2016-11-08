// Copyright (C) 2015 The Android Open Source Project
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

package org.eclipse.jgit.lfs.server.fs;

import com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository;

import org.apache.http.HttpStatus;
import org.eclipse.jgit.lfs.errors.InvalidLongObjectIdException;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.Constants;
import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.eclipse.jgit.lfs.server.internal.LfsServerText;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LfsFsReferencingContentServlet extends FileLfsServlet {
  private static final long serialVersionUID = 1L;

  private final FileLfsRepository repository;
  private final long timeout;

  public LfsFsReferencingContentServlet(LocalLargeFileRepository largeFileRepository) {
    super(largeFileRepository, 0);
    this.repository = largeFileRepository;
    this.timeout = 0;
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse rsp)
      throws ServletException, IOException {
    AnyLongObjectId id = getObjectToTransfer(req, rsp);
    if (id != null) {
        AsyncContext context = req.startAsync();
        context.setTimeout(timeout);
        req.getInputStream().setReadListener(new ObjectUploadListener(
                repository, context, req, rsp, id));
    }
  }

  private AnyLongObjectId getObjectToTransfer(HttpServletRequest req,
      HttpServletResponse rsp) throws IOException {
    String info = req.getPathInfo();
    if (info.length() != 1 + Constants.LONG_OBJECT_ID_STRING_LENGTH) {
      sendError(rsp, HttpStatus.SC_UNPROCESSABLE_ENTITY,
          MessageFormat.format(LfsServerText.get().invalidPathInfo, info));
      return null;
    }
    try {
      return LongObjectId.fromString(info.substring(1, 65));
    } catch (InvalidLongObjectIdException e) {
      sendError(rsp, HttpStatus.SC_UNPROCESSABLE_ENTITY, e.getMessage());
      return null;
    }
  }
}
