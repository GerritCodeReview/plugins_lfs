load("@npm_bazel_rollup//:index.bzl", "rollup_bundle")
load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)
load("//tools/bzl:genrule2.bzl", "genrule2")
load("//tools/bzl:js.bzl", "polygerrit_plugin")

LFS_DEPS = [
    "@jgit//org.eclipse.jgit.lfs.server:jgit-lfs-server",
    "@jgit//org.eclipse.jgit.lfs:jgit-lfs",
]

DEPLOY_ENV = [
    "//lib:gson",
    "//lib/httpcomponents:httpclient",
    "//lib:jgit",
    "//lib:servlet-api-without-neverlink",
]

# TODO(davido): Remove this workaround, when provided_deps attribute is added:
# https://github.com/bazelbuild/bazel/issues/1402
java_binary(
    name = "gerrit_core_provided_env",
    main_class = "Dummy",
    runtime_deps = DEPLOY_ENV,
)

gerrit_plugin(
    name = "lfs",
    srcs = glob(["src/main/java/**/*.java"]),
    deploy_env = ["gerrit_core_provided_env"],
    manifest_entries = [
        "Gerrit-PluginName: lfs",
        "Gerrit-Module: com.googlesource.gerrit.plugins.lfs.Module",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.lfs.HttpModule",
        "Gerrit-SshModule: com.googlesource.gerrit.plugins.lfs.SshModule",
        "Gerrit-InitStep: com.googlesource.gerrit.plugins.lfs.InitLfs",
    ],
    resource_jars = [":gr-lfs-static"],
    resources = glob(["src/main/resources/**/*"]),
    deps = LFS_DEPS,
)

genrule2(
    name = "gr-lfs-static",
    srcs = [":gr-lfs"],
    outs = ["gr-lfs-static.jar"],
    cmd = " && ".join([
        "mkdir $$TMP/static",
        "cp -r $(locations :gr-lfs) $$TMP/static",
        "cd $$TMP",
        "zip -Drq $$ROOT/$@ -g .",
    ]),
)

polygerrit_plugin(
    name = "gr-lfs",
    app = "gr-lfs-bundle.js",
    plugin_name = "gr-lfs",
)

rollup_bundle(
    name = "gr-lfs-bundle",
    srcs = glob(["gr-lfs/*.js"]),
    entry_point = "gr-lfs/plugin.js",
    format = "iife",
    rollup_bin = "//tools/node_tools:rollup-bin",
    sourcemap = "hidden",
    deps = [
        "@tools_npm//rollup-plugin-node-resolve",
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
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + LFS_DEPS + [
        ":lfs__plugin",
    ],
)
