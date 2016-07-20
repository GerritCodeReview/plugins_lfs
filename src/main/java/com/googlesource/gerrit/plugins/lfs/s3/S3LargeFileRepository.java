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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;

import org.eclipse.jgit.lfs.server.s3.S3Config;
import org.eclipse.jgit.lfs.server.s3.S3Repository;

public class S3LargeFileRepository extends S3Repository {

  @Inject
  S3LargeFileRepository(PluginConfigFactory cfg, @PluginName String pluginName) {
    super(getS3Config(cfg, pluginName));
  }

  private static S3Config getS3Config(PluginConfigFactory configFactory,
      String pluginName) {
    PluginConfig cfg = configFactory.getFromGerritConfig(pluginName);
    String region = cfg.getString("region", null);
    String bucket = cfg.getString("bucket", null);
    String storageClass = cfg.getString("storageClass", "REDUCED_REDUNDANCY");
    String accessKey = cfg.getString("accessKey", null);
    String secretKey = cfg.getString("secretKey", null);
    int expirationSeconds = cfg.getInt("expirationSeconds", 60);
    boolean disableSslVerify = cfg.getBoolean("disableSslVerify", false);

    return new S3Config(region, bucket, storageClass, accessKey, secretKey,
        expirationSeconds, disableSslVerify);
  }
}
