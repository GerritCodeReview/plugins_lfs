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
// TODO move file back to com.googlesource.gerrit.plugin.lfs.fs package when
// https://git.eclipse.org/r/#/c/84933/ is picked up by gerrit

import static com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository.DOWNLOAD;
import static com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository.UPLOAD;
import static org.eclipse.jgit.util.HttpSupport.HDR_AUTHORIZATION;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.googlesource.gerrit.plugins.lfs.fs.LfsFsRequestAuthorizer;
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

public class LfsFsContentServlet extends FileLfsServlet {
  public interface Factory {
    LfsFsContentServlet create(LocalLargeFileRepository largeFileRepository);
  }

  private static final long serialVersionUID = 1L;

  private final LfsFsRequestAuthorizer authorizer;
  private final LocalLargeFileRepository repository;
  private final long timeout;

  @Inject
  public LfsFsContentServlet(LfsFsRequestAuthorizer authorizer,
      @Assisted LocalLargeFileRepository repository) {
    super(repository, 0);
    this.authorizer = authorizer;
    this.repository = repository;
    this.timeout = 0;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse rsp)
      throws ServletException, IOException {
    AnyLongObjectId obj = getObjectToTransfer(req, rsp);
    if (obj == null) {
      return;
    }

    if (repository.getSize(obj) == -1) {
      sendError(rsp, HttpStatus.SC_NOT_FOUND, MessageFormat
          .format(LfsServerText.get().objectNotFound, obj.getName()));
      return;
    }

    if (!authorizer.verifyAuthInfo(req.getHeader(HDR_AUTHORIZATION),
        DOWNLOAD, obj)) {
      sendError(rsp, HttpStatus.SC_UNAUTHORIZED, MessageFormat.format(
          LfsServerText.get().failedToCalcSignature, "Invalid authorization token"));
      return;
    }

    AsyncContext context = req.startAsync();
    context.setTimeout(timeout);
    rsp.getOutputStream().setWriteListener(
        new ObjectDownloadListener(repository, context, rsp, obj));
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse rsp)
      throws ServletException, IOException {
    AnyLongObjectId id = getObjectToTransfer(req, rsp);
    if (id == null) {
      return;
    }

    if (!authorizer.verifyAuthInfo(
        req.getHeader(HDR_AUTHORIZATION), UPLOAD, id)) {
      sendError(rsp, HttpStatus.SC_UNAUTHORIZED,
          MessageFormat.format(LfsServerText.get().failedToCalcSignature,
              "Invalid authorization token"));
      return;
    }

    AsyncContext context = req.startAsync();
    context.setTimeout(timeout);
    req.getInputStream().setReadListener(
        new ObjectUploadListener(repository, context, req, rsp, id));
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
