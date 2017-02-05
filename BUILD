load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "lfs",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    manifest_entries = [
        "Gerrit-PluginName: lfs",
        "Gerrit-Module: com.googlesource.gerrit.plugins.lfs.Module",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.lfs.HttpModule",
        "Gerrit-SshModule: com.googlesource.gerrit.plugins.lfs.SshModule",
    ],
    deps = [
        "//plugins/lfs/lib/jgit:jgit-http-apache",
        "//plugins/lfs/lib/jgit:jgit-lfs",
        "//plugins/lfs/lib/jgit:jgit-lfs-server",
    ],
)

junit_tests(
    name = "lfs_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["lfs"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":lfs__plugin",
        "//plugins/lfs/lib/jgit:jgit-lfs",
    ],
)
