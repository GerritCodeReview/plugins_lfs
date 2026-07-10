load("@com_googlesource_gerrit_bazlets//:gerrit_plugin.bzl", "gerrit_plugin", "gerrit_plugin_tests")
load("@rules_java//java:defs.bzl", "java_binary")

LFS_DEPS = [
    "@jgit//org.eclipse.jgit.lfs:jgit-lfs",
    "@jgit//org.eclipse.jgit.lfs.server.ee8:jgit-lfs-server-ee8",
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
    srcs = glob(["src/main/java/**/*.java"]),
    deploy_env = ["gerrit_core_provided_env"],
    manifest_entries = [
        "Gerrit-PluginName: lfs",
        "Gerrit-Module: com.googlesource.gerrit.plugins.lfs.Module",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.lfs.HttpModule",
        "Gerrit-SshModule: com.googlesource.gerrit.plugins.lfs.SshModule",
        "Gerrit-InitStep: com.googlesource.gerrit.plugins.lfs.InitLfs",
    ],
    plugin = "lfs",
    resource_jars = ["//plugins/lfs/web:lfs"],
    resources = glob(["src/main/resources/**/*"]),
    deps = LFS_DEPS,
)

gerrit_plugin_tests(
    srcs = glob(["src/test/java/**/*.java"]),
    plugin = "lfs",
    deps = LFS_DEPS,
)
