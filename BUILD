load("//tools/bzl:plugin.bzl", "gerrit_plugin")

gerrit_plugin(
    name = "lfs",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        'Gerrit-PluginName: lfs',
        'Gerrit-Module: com.googlesource.gerrit.plugins.lfs.Module',
        'Gerrit-HttpModule: com.googlesource.gerrit.plugins.lfs.HttpModule',
        'Gerrit-SshModule: com.googlesource.gerrit.plugins.lfs.SshModule',
    ],
    resources = glob(["src/main/resources/**/*"]),
    deps = [
      "//lib/jgit:jgit-http-apache",
      "//lib/jgit:jgit-lfs",
      "//lib/jgit:jgit-lfs-server",
      "//lib/httpcomponents:httpcore",
    ],
)
