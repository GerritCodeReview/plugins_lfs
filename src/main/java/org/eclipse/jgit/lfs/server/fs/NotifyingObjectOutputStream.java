/*
 * Copyright 2016 CollabNet, Inc. All rights reserved.
 * http://www.collab.net
 */
package org.eclipse.jgit.lfs.server.fs;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;

import java.io.IOException;
import java.nio.file.Path;

public class NotifyingObjectOutputStream extends AtomicObjectOutputStream {
  private final LinkingFileLfsRepository repository;
  private final Path path;
  private final AnyLongObjectId id;

  private boolean aborted;

  NotifyingObjectOutputStream(LinkingFileLfsRepository repository,
      Path path, AnyLongObjectId id)
      throws IOException {
    super(path, id);
    this.repository = repository;
    this.path = path;
    this.id = id;
  }

  @Override
  public void close() throws IOException {
    super.close();
    if (!aborted) {
      repository.linkRepositoryToId(id, path);
    }
  }

  @Override
  void abort() {
    super.abort();
    this.aborted = true;
  }
}
