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
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.lfs.LfsBackend;
import com.googlesource.gerrit.plugins.lfs.LfsConfigurationFactory;
import com.googlesource.gerrit.plugins.lfs.LfsGlobalConfig;
import org.eclipse.jgit.lfs.server.s3.S3Config;
import org.eclipse.jgit.lfs.server.s3.S3Repository;

public class S3LargeFileRepository extends S3Repository {
  public interface Factory {
    S3LargeFileRepository create(LfsBackend backendConfig);
  }

  @Inject
  S3LargeFileRepository(LfsConfigurationFactory configFactory, @Assisted LfsBackend backendConfig) {
    super(getS3Config(configFactory.getGlobalConfig(), backendConfig));
  }

  private static S3Config getS3Config(LfsGlobalConfig config, LfsBackend backendConfig) {
    String section = backendConfig.type.name();
    String hostname = config.getString(section, backendConfig.name, "hostname");
    String region = config.getString(section, backendConfig.name, "region");
    String bucket = config.getString(section, backendConfig.name, "bucket");
    String storageClass =
        MoreObjects.firstNonNull(
            config.getString(section, backendConfig.name, "storageClass"), "REDUCED_REDUNDANCY");
    int expirationSeconds = config.getInt(section, backendConfig.name, "expirationSeconds", 60);
    boolean disableSslVerify =
        config.getBoolean(section, backendConfig.name, "disableSslVerify", false);

    String accessKey = config.getString(section, backendConfig.name, "accessKey");
    String secretKey = config.getString(section, backendConfig.name, "secretKey");

    if (Strings.isNullOrEmpty(hostname)) {
      return new S3Config(
          hostname, region, bucket, storageClass, accessKey, secretKey, expirationSeconds,
          disableSslVerify);
    }

    return new S3Config(
        region, bucket, storageClass, accessKey, secretKey, expirationSeconds, disableSslVerify);
  }
}
