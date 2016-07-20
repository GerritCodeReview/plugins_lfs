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
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;

import org.eclipse.jgit.lfs.server.s3.S3Config;
import org.eclipse.jgit.lfs.server.s3.S3Repository;
import org.eclipse.jgit.lib.Config;

public class S3LargeFileRepository extends S3Repository {

  @Inject
  S3LargeFileRepository(PluginConfigFactory cfg, @PluginName String pluginName) {
    super(getS3Config(cfg, pluginName));
  }

  private static S3Config getS3Config(PluginConfigFactory configFactory,
      String pluginName) {
    Config pluginCfg = configFactory.getGlobalPluginConfig(pluginName);
    String section = "s3";
    String region = pluginCfg.getString(section, null, "region");
    String bucket = pluginCfg.getString(section, null, "bucket");
    String storageClass =
        MoreObjects.firstNonNull(
            pluginCfg.getString(section, null, "storageClass"),
            "REDUCED_REDUNDANCY");
    int expirationSeconds =
        pluginCfg.getInt(section, null, "expirationSeconds", 60);
    boolean disableSslVerify =
        pluginCfg.getBoolean(section, null, "disableSslVerify", false);

    PluginConfig cfg = configFactory.getFromGerritConfig(pluginName);
    String accessKey = cfg.getString("accessKey", null);
    String secretKey = cfg.getString("secretKey", null);

    return new S3Config(region, bucket, storageClass, accessKey, secretKey,
        expirationSeconds, disableSslVerify);
  }
}
