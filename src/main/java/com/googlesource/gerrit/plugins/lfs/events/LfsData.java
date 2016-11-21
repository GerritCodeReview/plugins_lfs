// Copyright (C) 2016 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.lfs.events;

public class LfsData {
  static class Builder {
    private String backend;
    private String project;
    private String commit;
    private String oid;
    private Long size;

    LfsData build() {
      return new LfsData(backend, project, commit, oid, size);
    }

    boolean isValid() {
      return oid != null && size != null;
    }

    Builder withBackend(String backend) {
      this.backend = backend;
      return this;
    }

    Builder withProject(String project) {
      this.project = project;
      return this;
    }

    Builder withCommit(String commit) {
      this.commit = commit;
      return this;
    }

    Builder withOid(String oid) {
      this.oid = oid;
      return this;
    }

    Builder withSize(Long size) {
      this.size = size;
      return this;
    }
  }

  public final String key;
  public final String backend;
  public final String project;
  public final String commit;
  public final String oid;
  public final Long size;

  private LfsData(String backend, String project,
      String commit, String oid, Long size) {
    this.key = String.format("%s_%s", backend, oid);
    this.backend = backend;
    this.project = project;
    this.commit = commit;
    this.oid = oid;
    this.size = size;
  }
}
