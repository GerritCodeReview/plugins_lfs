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

package org.eclipse.jgit.lfs.server.fs;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.googlesource.gerrit.plugins.lfs.fs.NamedFileLfsRepository;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.lfs.errors.CorruptLongObjectException;
import org.eclipse.jgit.lfs.internal.AtomicObjectOutputStream;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class has to be in org.eclipse.jgit.lfs.server.fs package so that it has an access to
 * FileLfsRepository.getPath() method which is package protected.
 */
public class ObjectUploadListenerWithPostUploadHook implements ReadListener {
  public interface LfsPostUploadHook {
    void onPostUpload(String lfsRepository, String filePath, long size);
  }

  private static Logger log = LoggerFactory.getLogger(ObjectUploadListenerWithPostUploadHook.class);

  private final AsyncContext context;
  private final HttpServletResponse response;
  private final ServletInputStream in;
  private final ReadableByteChannel inChannel;
  private final AtomicObjectOutputStream out;
  private final WritableByteChannel channel;
  private final DynamicSet<LfsPostUploadHook> postUploads;
  private final String repository;
  private final ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
  private final Path path;
  private long uploaded;

  public ObjectUploadListenerWithPostUploadHook(
      NamedFileLfsRepository repository,
      AsyncContext context,
      HttpServletRequest request,
      HttpServletResponse response,
      AnyLongObjectId id,
      DynamicSet<LfsPostUploadHook> postUploads)
      throws FileNotFoundException, IOException {
    this.context = context;
    this.response = response;
    this.postUploads = postUploads;
    this.in = request.getInputStream();
    this.inChannel = Channels.newChannel(in);
    this.path = repository.getPath(id);
    this.out = getOutputStream(repository, id);
    this.channel = Channels.newChannel(out);
    this.repository = repository.name();
    this.uploaded = 0L;
    response.setContentType(Constants.CONTENT_TYPE_GIT_LFS_JSON);
  }

  @Override
  public void onAllDataRead() throws IOException {
    close();
  }

  @Override
  public void onDataAvailable() throws IOException {
    while (in.isReady()) {
      if (inChannel.read(buffer) > 0) {
        buffer.flip();
        uploaded += Integer.valueOf(channel.write(buffer)).longValue();
        buffer.compact();
      } else {
        buffer.flip();
        while (buffer.hasRemaining()) {
          uploaded += Integer.valueOf(channel.write(buffer)).longValue();
        }
        close();
        return;
      }
    }
  }

  @Override
  public void onError(Throwable e) {
    try {
      out.abort();
      inChannel.close();
      channel.close();
      int status;
      if (e instanceof CorruptLongObjectException) {
        status = HttpStatus.SC_BAD_REQUEST;
        log.warn(e.getMessage(), e);
      } else {
        status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        log.error(e.getMessage(), e);
      }
      FileLfsServlet.sendError(response, status, e.getMessage());
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
    }
  }

  private void close() throws IOException {
    try {
      inChannel.close();
      channel.close();
      if (!response.isCommitted()) {
        response.setStatus(HttpServletResponse.SC_OK);
      }
      for (LfsPostUploadHook postUploadHook : postUploads) {
        postUploadHook.onPostUpload(repository, path.toString(), uploaded);
      }
    } finally {
      context.complete();
    }
  }

  private AtomicObjectOutputStream getOutputStream(FileLfsRepository repo, AnyLongObjectId id)
      throws IOException {
    Path parent = path.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    return new AtomicObjectOutputStream(path, id);
  }
}
