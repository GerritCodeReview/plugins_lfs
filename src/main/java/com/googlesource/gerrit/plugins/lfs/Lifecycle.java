package com.googlesource.gerrit.plugins.lfs;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Lifecycle implements LifecycleListener {
  private static final Logger log = LoggerFactory.getLogger(Lifecycle.class);

  private final String name;
  private final Config config;

  @Inject
  Lifecycle(@PluginName String name, @GerritServerConfig Config config) {
    this.name = name;
    this.config = config;
  }

  @Override
  public void start() {
    String plugin = config.getString("lfs", null, "plugin");
    if (Strings.isNullOrEmpty(plugin)) {
      warn("lfs.plugin is not set");
    } else if (!plugin.equals(name)) {
      warn(String.format("lfs.plugin is set, but is not set to '%s'", name));
    }
  }

  private void warn(String msg) {
    log.warn(String.format(
        "%s; LFS will not be enabled. Run site initialization, or manually set"
        + " lfs.plugin to '%s' in gerrit.config", msg, name));
  }

  @Override
  public void stop() {
  }
}
