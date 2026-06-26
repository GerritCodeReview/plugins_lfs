load("@com_googlesource_gerrit_bazlets//:gerrit_plugin.bzl", "gerrit_plugin", "gerrit_plugin_tests")

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
    resource_jars = ["//plugins/lfs/web:lfs"],
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "@jgit//org.eclipse.jgit.lfs:jgit-lfs",
        "@jgit//org.eclipse.jgit.lfs.server.ee8:jgit-lfs-server-ee8",
    ],
)

gerrit_plugin_tests(
    name = "lfs_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["lfs"],
    deps = [
        ":lfs__plugin",
        "@jgit//org.eclipse.jgit.lfs:jgit-lfs",
        "@jgit//org.eclipse.jgit.lfs.server.ee8:jgit-lfs-server-ee8",
    ],
)
