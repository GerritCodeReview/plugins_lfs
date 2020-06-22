workspace(name = "lfs")

load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "eb6506b018b174db297c24ec78e86802f1102559",
    #local_path = "/home/<user>/projects/bazlets",
)

load(
    "@com_googlesource_gerrit_bazlets//:gerrit_api.bzl",
    "gerrit_api",
)

gerrit_api()

load(":external_plugin_deps.bzl", "external_plugin_deps")

external_plugin_deps(use_lfs_from_gerrit_tree = False)
