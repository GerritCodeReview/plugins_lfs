load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

gerrit_plugin(
    name = "lfs",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: lfs",
        "Gerrit-Module: com.googlesource.gerrit.plugins.lfs.Module",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.lfs.HttpModule",
        "Gerrit-SshModule: com.googlesource.gerrit.plugins.lfs.SshModule",
        "Gerrit-InitStep: com.googlesource.gerrit.plugins.lfs.InitLfs",
    ],
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "@jgit_http_apache//jar",
        "@jgit_lfs//jar",
        "@jgit_lfs_server//jar",
    ],
)

junit_tests(
    name = "lfs_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["lfs"],
    deps = [
        ":lfs__plugin_test_deps",
    ],
)

java_library(
    name = "lfs__plugin_test_deps",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":lfs__plugin",
        "@jgit_lfs//jar",
        "@joda_time//jar",
    ],
)
