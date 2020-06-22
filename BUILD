load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "IN_TREE_BUILD_MODE",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

LFS_DEPS = [
    "@jgit//org.eclipse.jgit.lfs.server:jgit-lfs-server",
    "@jgit//org.eclipse.jgit.lfs:jgit-lfs",
] if IN_TREE_BUILD_MODE else [
    "@jgit-http-apache//jar",
    "@jgit-lfs-server//jar",
    "@jgit-lfs//jar",
]

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
    deps = LFS_DEPS,
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
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + LFS_DEPS + [
        ":lfs__plugin",
        "@joda-time//jar",
    ],
)
