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

package com.googlesource.gerrit.plugins.lfs.s3;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsConfig;

import org.eclipse.jgit.lfs.server.s3.S3Config;
import org.eclipse.jgit.lfs.server.s3.S3Repository;
import org.eclipse.jgit.lib.Config;

public class S3LargeFileRepository extends S3Repository {

  @Inject
  S3LargeFileRepository(LfsConfig.Factory configFactory) {
    super(getS3Config(configFactory.create().getGlobalConfig()));
  }

  private static S3Config getS3Config(Config config) {
    String section = LfsBackend.S3.name();
    String region = config.getString(section, null, "region");
    String bucket = config.getString(section, null, "bucket");
    String storageClass =
        MoreObjects.firstNonNull(
            config.getString(section, null, "storageClass"),
            "REDUCED_REDUNDANCY");
    int expirationSeconds =
        config.getInt(section, null, "expirationSeconds", 60);
    boolean disableSslVerify =
        config.getBoolean(section, null, "disableSslVerify", false);

    String accessKey = config.getString(section, null, "accessKey");
    String secretKey = config.getString(section, null, "secretKey");

    return new S3Config(region, bucket, storageClass, accessKey, secretKey,
        expirationSeconds, disableSslVerify);
  }
}
