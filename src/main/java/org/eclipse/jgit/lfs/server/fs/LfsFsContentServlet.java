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

import static com.google.gerrit.extensions.restapi.Url.decode;

import com.google.common.base.Optional;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.googlesource.gerrit.plugins.lfs.fs.LocalLargeFileRepository;

import org.apache.http.HttpStatus;
import org.eclipse.jgit.lfs.errors.InvalidLongObjectIdException;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.Constants;
import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.eclipse.jgit.lfs.server.internal.LfsServerText;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true)
public class LfsFsContentServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final LocalLargeFileRepository repository;
  private final long timeout;
  private static Gson gson = createGson();

  public LfsFsContentServlet(LocalLargeFileRepository largeFileRepository) {
    this.repository = largeFileRepository;
    this.timeout = 0;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse rsp)
      throws ServletException, IOException {
    String path = req.getPathInfo();
    Optional<String> project = getProject(path, rsp);
    if (!project.isPresent()) {
      return;
    }

    path = path.substring(project.get().length() + 1);
    Optional<AnyLongObjectId> obj = getObjectToTransfer(path, rsp);
    if (obj.isPresent()) {
      LinkingFileLfsRepository projectRepository =
          repository.getProjectRepository(decode(project.get()));
      if (projectRepository.getSize(obj.get()) == -1) {
        sendError(rsp, HttpStatus.SC_NOT_FOUND, MessageFormat
            .format(LfsServerText.get().objectNotFound, obj.get().getName()));
        return;
      }
      AsyncContext context = req.startAsync();
      context.setTimeout(timeout);
      rsp.getOutputStream().setWriteListener(
          new ObjectDownloadListener(
              projectRepository, context, rsp, obj.get()));
    }
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse rsp)
      throws ServletException, IOException {
    String path = req.getPathInfo();
    Optional<String> project = getProject(path, rsp);
    if (!project.isPresent()) {
      return;
    }

    path = path.substring(project.get().length() + 1);
    Optional<AnyLongObjectId> id = getObjectToTransfer(path, rsp);
    if (id.isPresent()) {
        AsyncContext context = req.startAsync();
        context.setTimeout(timeout);
        req.getInputStream().setReadListener(new ObjectUploadListener(
                repository.getProjectRepository(decode(project.get())),
                context, req, rsp, id.get()));
    }
  }

  private Optional<String> getProject(String path, HttpServletResponse rsp)
      throws IOException {
    int slash = path.lastIndexOf('/');
    if (slash < 1) {
      sendError(rsp, HttpStatus.SC_UNPROCESSABLE_ENTITY,
          MessageFormat.format(LfsServerText.get().invalidPathInfo, path));
      return Optional.absent();
    }
    return Optional.of(path.substring(1, slash));
  }

  private Optional<AnyLongObjectId> getObjectToTransfer(String path,
      HttpServletResponse rsp) throws IOException {
    int length = 1 + Constants.LONG_OBJECT_ID_STRING_LENGTH;
    if (path.length() != length) {
      sendError(rsp, HttpStatus.SC_UNPROCESSABLE_ENTITY,
          MessageFormat.format(LfsServerText.get().invalidPathInfo, path));
      return Optional.absent();
    }
    try {
      return Optional.<AnyLongObjectId>of(
          LongObjectId.fromString(path.substring(1, length)));
    } catch (InvalidLongObjectIdException e) {
      sendError(rsp, HttpStatus.SC_UNPROCESSABLE_ENTITY, e.getMessage());
      return Optional.absent();
    }
  }

  static class Error {
    String message;

    Error(String m) {
      this.message = m;
    }
  }

  static void sendError(HttpServletResponse rsp, int status, String message)
      throws IOException {
    rsp.setStatus(status);
    try (PrintWriter writer = rsp.getWriter()) {
      gson.toJson(new Error(message), writer);
      writer.flush();
    }
    rsp.flushBuffer();
  }

  private static Gson createGson() {
    GsonBuilder gb = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .setPrettyPrinting().disableHtmlEscaping();
    return gb.create();
  }
}
