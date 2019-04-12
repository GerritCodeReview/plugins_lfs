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

package com.googlesource.gerrit.plugins.lfs;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Singleton;
import java.io.Reader;

@Singleton
public class LfsGson {
  private final Gson gson;

  LfsGson() {
    this.gson =
        new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .disableHtmlEscaping()
            .create();
  }

  public void toJson(Object src, Appendable writer) throws JsonIOException {
    gson.toJson(src, writer);
  }

  public String toJson(Object src) {
    return gson.toJson(src);
  }

  public <T> T fromJson(Reader json, Class<T> classOfT)
      throws JsonSyntaxException, JsonIOException {
    return gson.fromJson(json, classOfT);
  }
}
