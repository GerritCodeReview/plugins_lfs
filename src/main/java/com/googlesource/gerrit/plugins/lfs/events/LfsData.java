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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class LfsData {
  public static class Builder {
    private String backend;
    private Iterable<String> projects;
    private String commit;
    private String oid;
    private Long size;

    public LfsData build() {
      return new LfsData(backend, projects, commit, oid, size);
    }

    public Builder withKey(String key) {
      int divider = -1;
      if (Strings.isNullOrEmpty(key)
          || (divider = key.lastIndexOf('_')) == -1) {
        return this;
      }
      return withOid(key.substring(divider + 1))
          .withBackend(key.substring(0, divider));
    }

    public Builder withBackend(String backend) {
      this.backend = backend;
      return this;
    }

    public Builder withProjects(Iterable<String> projects) {
      this.projects = projects;
      return this;
    }

    public Builder withProject(String project) {
      this.projects = ImmutableList.of(project);
      return this;
    }

    public Builder withSize(Long size) {
      this.size = size;
      return this;
    }

    Builder withCommit(String commit) {
      this.commit = commit;
      return this;
    }

    boolean isValid() {
      return oid != null && size != null;
    }

    Builder withOid(String oid) {
      this.oid = oid;
      return this;
    }
  }

  public final String key;
  public final String backend;
  public final Iterable<String> projects;
  public final String commit;
  public final Long size;

  private LfsData(String backend, Iterable<String> projects,
      String commit, String oid, Long size) {
    this.key = String.format("%s_%s", backend, oid);
    this.backend = backend;
    this.projects = projects;
    this.commit = commit;
    this.size = size;
  }
}
