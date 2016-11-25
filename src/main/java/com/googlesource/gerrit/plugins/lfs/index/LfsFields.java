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

package com.googlesource.gerrit.plugins.lfs.index;

import com.google.gerrit.server.index.FieldDef;
import com.google.gerrit.server.index.FieldType;
import com.google.gwtorm.server.OrmException;

import com.googlesource.gerrit.plugins.lfs.events.LfsData;

public class LfsFields {
  public static final FieldDef<LfsData, String> KEY =
      new FieldDef.Single<LfsData, String>("key",
          FieldType.EXACT, true) {
        @Override
        public String get(LfsData input, FillArgs args)
            throws OrmException {
          return input.key;
        }
      };

  public static final FieldDef<LfsData, String> BACKEND =
      new FieldDef.Single<LfsData, String>("backend",
          FieldType.EXACT, true) {
        @Override
        public String get(LfsData input, FillArgs args)
            throws OrmException {
          return input.backend;
        }
      };

  public static final FieldDef<LfsData, String> PROJECT =
      new FieldDef.Single<LfsData, String>("project",
          FieldType.EXACT, true) {
        @Override
        public String get(LfsData input, FillArgs args)
            throws OrmException {
          return input.project;
        }
      };

  public static final FieldDef<LfsData, String> OID =
      new FieldDef.Single<LfsData, String>("oid",
          FieldType.EXACT, true) {
        @Override
        public String get(LfsData input, FillArgs args)
            throws OrmException {
          return input.oid;
        }
      };

  public static final FieldDef<LfsData, Long> SIZE =
      new FieldDef.Single<LfsData, Long>("size",
          FieldType.LONG, true) {
        @Override
        public Long get(LfsData input, FillArgs args)
            throws OrmException {
          return input.size;
        }
      };
}
