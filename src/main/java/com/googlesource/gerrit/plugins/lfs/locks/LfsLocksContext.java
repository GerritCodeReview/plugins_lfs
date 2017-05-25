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

import static com.google.gerrit.extensions.api.lfs.LfsDefinitions.CONTENTTYPE_VND_GIT_LFS_JSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpStatus.SC_OK;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LfsLocksContext {
  private static final Logger log = LoggerFactory.getLogger(LfsLocksContext.class);

  public final String path;

  private final HttpServletRequest req;
  private final HttpServletResponse res;
  private final Supplier<Writer> writer;
  private final Supplier<Reader> reader;
  private final Gson gson;

  LfsLocksContext(final HttpServletRequest req, final HttpServletResponse res) {
    this.path = req.getPathInfo().startsWith("/") ? req.getPathInfo() : "/" + req.getPathInfo();
    this.req = req;
    this.res = res;
    this.writer =
        Suppliers.memoize(
            new Supplier<Writer>() {
              @Override
              public Writer get() {
                try {
                  return new BufferedWriter(new OutputStreamWriter(res.getOutputStream(), UTF_8));
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              }
            });
    this.reader =
        Suppliers.memoize(
            new Supplier<Reader>() {
              @Override
              public Reader get() {
                try {
                  return new BufferedReader(new InputStreamReader(req.getInputStream(), UTF_8));
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              }
            });
    this.gson = createGson();
    setLfsResponseType();
  }

  String getHeader(String name) {
    return req.getHeader(name);
  }

  String getParam(String name) {
    return req.getParameter(name);
  }

  <T> T input(Class<T> clazz) {
    return gson.fromJson(getReader(), clazz);
  }

  <T> void sendResponse(T content) throws IOException {
    res.setStatus(SC_OK);
    gson.toJson(content, getWriter());
    getWriter().flush();
  }

  void sendError(int status, String message) throws IOException {
    sendError(status, new Error(message));
  }

  void sendError(int status, Error error) throws IOException {
    log.error(error.message);
    res.setStatus(status);
    gson.toJson(error, getWriter());
    getWriter().flush();
  }

  Writer getWriter() {
    return writer.get();
  }

  Reader getReader() {
    return reader.get();
  }

  void setLfsResponseType() {
    res.setContentType(CONTENTTYPE_VND_GIT_LFS_JSON);
  }

  private Gson createGson() {
    return new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .disableHtmlEscaping()
        .create();
  }

  /** copied from org.eclipse.jgit.lfs.server.LfsProtocolServlet.Error */
  static class Error {
    String message;

    Error(String m) {
      this.message = m;
    }
  }
}
