/*
 * Copyright 2016 CollabNet, Inc. All rights reserved.
 * http://www.collab.net
 */
package org.eclipse.jgit.lfs.server.fs;

import org.eclipse.jgit.lfs.lib.AnyLongObjectId;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LinkingFileLfsRepository extends FileLfsRepository {
  private final Path repository;

  public LinkingFileLfsRepository(String url, Path dir, String project) throws IOException {
    super(url, dir);
    this.repository = Paths.get(getDir().toString(), "repositories", project);
  }

  @Override
  AtomicObjectOutputStream getOutputStream(AnyLongObjectId id)
      throws IOException {
    Path path = getPath(id);
    Path parent = path.getParent();
    if (parent != null) {
        Files.createDirectories(parent);
    }
    return new NotifyingObjectOutputStream(this, path, id);
  }

  public void linkRepositoryToId(AnyLongObjectId id, Path path)
      throws IOException {
    String oid = id.name();
    Path linkDir = getLinkDir(oid);
    Path link = Paths.get(linkDir.toString(), oid);
    if (Files.exists(link)) {
      return;
    }

    Files.createDirectories(linkDir);
    try {
      Files.createSymbolicLink(link, link.relativize(path));
    } catch (FileAlreadyExistsException e) {
      // file was created between check and creation attempt
    }
  }

  @Override
  public long getSize(AnyLongObjectId id) throws IOException {
    long size = super.getSize(id);
    if (size != -1) {
      // check if given repository has already reference to this object
      linkRepositoryToId(id, getPath(id));
    }
    return size;
  }

  private Path getLinkDir(String oid) {
    String parent = oid.substring(0, 2);
    String child = oid.substring(2, 4);
    Path linkDir = Paths.get(repository.toString(), parent, child);
    return linkDir;
  }
}
