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

import static com.googlesource.gerrit.plugins.lfs.LfsPaths.LFS_LOCKS_PATH;
import static com.googlesource.gerrit.plugins.lfs.LfsPaths.LFS_VERIFICATION_PATH;
import static com.googlesource.gerrit.plugins.lfs.LfsPaths.URL_REGEX_TEMPLATE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Singleton;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LfsLocksServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final String CONTENTTYPE_VND_GIT_LFS_JSON =
      "application/vnd.git-lfs+json; charset=utf-8";
  private static final Pattern LFS_LOCKS_URL =
      Pattern.compile(String.format(URL_REGEX_TEMPLATE, LFS_LOCKS_PATH));
  private static final Pattern LFS_VERIFICATION_URL =
      Pattern.compile(String.format(URL_REGEX_TEMPLATE, LFS_VERIFICATION_PATH));
  private static final Logger log = LoggerFactory.getLogger(LfsLocksServlet.class);
  static final DateTimeFormatter ISO = ISODateTimeFormat.dateTime();

  public static final String LFS_LOCKS_REST =
      String.format(URL_REGEX_TEMPLATE, LFS_LOCKS_PATH + "|" + LFS_VERIFICATION_PATH);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Action action = new Action(req, resp);
    Matcher matcher = LFS_LOCKS_URL.matcher(action.path);
    if (matcher.matches()) {
      String project = matcher.group(1);
      listLocks(project, action);
      return;
    }

    action.sendError(
        SC_INTERNAL_SERVER_ERROR, String.format("Unsupported path %s was provided", action.path));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Action action = new Action(req, resp);
    Matcher matcher = LFS_LOCKS_URL.matcher(action.path);
    if (matcher.matches()) {
      String project = matcher.group(1);
      String lockId = matcher.group(2);
      if (Strings.isNullOrEmpty(lockId)) {
        createLock(project, action);
      } else {
        deleteLock(project, lockId, action);
      }
      return;
    }

    matcher = LFS_VERIFICATION_URL.matcher(action.path);
    if (matcher.matches()) {
      verifyLocks(matcher.group(1), action);
      return;
    }

    action.sendError(
        SC_INTERNAL_SERVER_ERROR, String.format("Unsupported path %s was provided", action.path));
  }

  private void verifyLocks(String project, Action action) throws IOException {
    log.debug("Verify list of locks for {} project", project);
    //TODO method stub for verifying locks
    action.sendResponse(
        new LfsVerifyLocksResponse(Collections.emptyList(), Collections.emptyList(), null));
  }

  private void listLocks(String project, Action action) throws IOException {
    LfsListLocksInput input = action.input(LfsListLocksInput.class);
    log.debug("Get list of locks for {} project", project);
    //TODO method stub for getting project's locks list
    action.sendResponse(new LfsGetLocksResponse(Collections.emptyList(), input.cursor));
  }

  private void deleteLock(String project, String lockId, Action action) throws IOException {
    LfsDeleteLockInput input = action.input(LfsDeleteLockInput.class);
    log.debug(
        "Delete (-f {}) lock for {} in project {}",
        Boolean.TRUE.equals(input.force),
        lockId,
        project);
    //TODO: this is just the method stub for lock deletion
    LfsLock lock =
        new LfsLock(
            "random_id",
            "some/path/to/file",
            now(),
            new LfsLockOwner("Lock Owner <lock_owner@example.com>"));
    action.sendResponse(lock);
  }

  private void createLock(String project, Action action) throws IOException {
    LfsCreateLockInput input = action.input(LfsCreateLockInput.class);
    log.debug("Create lock for {} in project {}", input.path, project);
    //TODO: this is just the method stub lock creation
    LfsLock lock =
        new LfsLock(
            "random_id",
            input.path,
            now(),
            new LfsLockOwner("Lock Owner <lock_owner@example.com>"));
    action.sendResponse(lock);
  }

  private String now() {
    return ISO.print(DateTime.now().toDateTime(DateTimeZone.UTC));
  }

  private class Action {
    public final String path;

    private final HttpServletResponse res;
    private final Supplier<Writer> writer;
    private final Supplier<Reader> reader;
    private final Gson gson;

    private Action(final HttpServletRequest req, final HttpServletResponse res) {
      this.path = req.getPathInfo().startsWith("/") ? req.getPathInfo() : "/" + req.getPathInfo();
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

    <T> T input(Class<T> clazz) {
      return gson.fromJson(getReader(), clazz);
    }

    <T> void sendResponse(T content) throws IOException {
      res.setStatus(SC_OK);
      gson.toJson(content, getWriter());
      getWriter().flush();
    }

    void sendError(int status, String message) throws IOException {
      log.error(message);
      res.setStatus(status);
      gson.toJson(new Error(message), getWriter());
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
  }

  /** copied from org.eclipse.jgit.lfs.server.LfsProtocolServlet.Error */
  static class Error {
    String message;

    Error(String m) {
      this.message = m;
    }
  }
}
