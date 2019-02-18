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

package com.googlesource.gerrit.plugins.lfs.fs;

import static org.eclipse.jgit.lfs.lib.Constants.DOWNLOAD;
import static org.eclipse.jgit.lfs.lib.Constants.UPLOAD;
import static org.eclipse.jgit.util.HttpSupport.HDR_AUTHORIZATION;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.lfs.errors.InvalidLongObjectIdException;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.Constants;
import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.eclipse.jgit.lfs.server.fs.FileLfsServlet;
import org.eclipse.jgit.lfs.server.fs.ObjectDownloadListener;
import org.eclipse.jgit.lfs.server.internal.LfsServerText;

public class LfsFsRepoContentServlet extends FileLfsServlet {
  public interface Factory {
    LfsFsRepoContentServlet create(LocalProjectBackendLargeFileRepository largeFileRepository);
  }

  private static final long serialVersionUID = 1L;

  private final LfsFsRequestAuthorizer authorizer;
  private final UploadListenerProvider uploadListener;
  private final LocalProjectBackendLargeFileRepository repository;
  private final long timeout;

  @Inject
  public LfsFsRepoContentServlet(
      LfsFsRequestAuthorizer authorizer,
      UploadListenerProvider uploadListener,
      @Assisted LocalProjectBackendLargeFileRepository repository) {
    super(repository, 0);
    this.authorizer = authorizer;
    this.uploadListener = uploadListener;
    this.repository = repository;
    this.timeout = 0;
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse rsp)
      throws ServletException, IOException {
    String verifyId = req.getHeader(HttpHeaders.IF_NONE_MATCH);
    if (Strings.isNullOrEmpty(verifyId)) {
      doGet(req, rsp);
      return;
    }

    Optional<Context> ctx = validateGetRequest(req, rsp);
    if (ctx.isPresent() && ctx.get().id.getName().equalsIgnoreCase(verifyId)) {
      rsp.addHeader(HttpHeaders.ETAG, ctx.get().id.getName());
      rsp.setStatus(HttpStatus.SC_NOT_MODIFIED);
      return;
    }

    getObject(req, rsp, ctx);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse rsp)
      throws ServletException, IOException {
    Optional<Context> ctx = validateGetRequest(req, rsp);
    getObject(req, rsp, ctx);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse rsp)
      throws ServletException, IOException {
    Context ctx = getContext(req, rsp);
    if (ctx == null) {
      return;
    }

    if (!authorizer.verifyAuthInfo(req.getHeader(HDR_AUTHORIZATION), UPLOAD, ctx.id)) {
      sendError(
          rsp,
          HttpStatus.SC_UNAUTHORIZED,
          MessageFormat.format(
              LfsServerText.get().failedToCalcSignature, "Invalid authorization token"));
      return;
    }

    AsyncContext context = req.startAsync();
    context.setTimeout(timeout);
    req.getInputStream().setReadListener(uploadListener.get(ctx.repo, context, req, rsp, ctx.id));
  }

  protected Context getContext(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
    String info = req.getPathInfo();
    int length = 2 + Constants.LONG_OBJECT_ID_STRING_LENGTH;
    int slash = info.lastIndexOf('/');
    if (info.length() < length || slash == -1) {
      sendError(
          rsp,
          HttpStatus.SC_UNPROCESSABLE_ENTITY,
          MessageFormat.format(LfsServerText.get().invalidPathInfo, info));
      return null;
    }
    try {
      String id = info.substring(slash + 1, slash + 1 + Constants.LONG_OBJECT_ID_STRING_LENGTH);
      return new Context(
          repository.getRepository(info.substring(1, slash)), LongObjectId.fromString(id));
    } catch (InvalidLongObjectIdException e) {
      sendError(rsp, HttpStatus.SC_UNPROCESSABLE_ENTITY, e.getMessage());
      return null;
    }
  }

  private Optional<Context> validateGetRequest(HttpServletRequest req, HttpServletResponse rsp)
      throws IOException {
    Context ctx = getContext(req, rsp);
    if (ctx == null) {
      return Optional.empty();
    }

    if (ctx.repo.getSize(ctx.id) == -1) {
      sendError(
          rsp,
          HttpStatus.SC_NOT_FOUND,
          MessageFormat.format(LfsServerText.get().objectNotFound, ctx.id.getName()));
      return Optional.empty();
    }

    if (!authorizer.verifyAuthInfo(req.getHeader(HDR_AUTHORIZATION), DOWNLOAD, ctx.id)) {
      sendError(
          rsp,
          HttpStatus.SC_UNAUTHORIZED,
          MessageFormat.format(
              LfsServerText.get().failedToCalcSignature, "Invalid authorization token"));
      return Optional.empty();
    }
    return Optional.of(ctx);
  }

  private void getObject(HttpServletRequest req, HttpServletResponse rsp, Optional<Context> ctx)
      throws IOException {
    if (ctx.isPresent()) {
      AsyncContext context = req.startAsync();
      context.setTimeout(timeout);
      rsp.getOutputStream()
          .setWriteListener(new ObjectDownloadListener(ctx.get().repo, context, rsp, ctx.get().id));
    }
  }

  private class Context {
    private final NamedFileLfsRepository repo;
    private final AnyLongObjectId id;

    private Context(NamedFileLfsRepository repo, AnyLongObjectId id) {
      this.repo = repo;
      this.id = id;
    }
  }
}
