workspace(name = "lfs")

load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "8c91ef43828699c7ed33976897991a427dcfe04b",
    #    local_path = "/home/<user>/projects/bazlets",
)

# Release Plugin API
#load(
#    "@com_googlesource_gerrit_bazlets//:gerrit_api.bzl",
#    "gerrit_api",
#)

# Snapshot Plugin API
load(
    "@com_googlesource_gerrit_bazlets//:gerrit_api_maven_local.bzl",
    "gerrit_api_maven_local",
)

# Load release Plugin API
#gerrit_api()

# Load snapshot Plugin API
gerrit_api_maven_local()

load(":external_plugin_deps.bzl", "external_plugin_deps")

external_plugin_deps()
