workspace(name = "lfs")

load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "636deeb7706ad232ac4717304eec429ab1be6f7f",
    #    local_path = "/home/<user>/projects/bazlets",
)

load("@com_googlesource_gerrit_bazlets//tools:maven_jar.bzl",
     "maven_jar",
     "GERRIT",
     "MAVEN_CENTRAL")

JGIT_VERS = "4.8.0.201706111038-r"

JGIT_REPO = MAVEN_CENTRAL

maven_jar(
    name = "jgit_http_apache",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERS,
    sha1 = "78e55a9537e5f6b6fc196d72ad74009550dd3ed9",
    repository = JGIT_REPO,
)

maven_jar(
    name = "jgit_lfs",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs:" + JGIT_VERS,
    sha1 = "7f9906508cb129022120a998bdfb662748a52a79",
    repository = JGIT_REPO,
)

maven_jar(
    name = "jgit_lfs_server",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs.server:" + JGIT_VERS,
    sha1 = "d0631e2b55aeb41ddad167849f33f53a7eb58726",
    repository = JGIT_REPO,
)

# Release Plugin API
#load("@com_googlesource_gerrit_bazlets//:gerrit_api.bzl",
#     "gerrit_api")

# Snapshot Plugin API
load(
    "@com_googlesource_gerrit_bazlets//:gerrit_api_maven_local.bzl",
    "gerrit_api_maven_local",
)

# Load release Plugin API
#gerrit_api()

# Load snapshot Plugin API
gerrit_api_maven_local()
