workspace(name = "lfs")

load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "28aa2290c7f7742261d69b358f3de30d2e87c13b",
    #    local_path = "/home/<user>/projects/bazlets",
)

load("@com_googlesource_gerrit_bazlets//tools:maven_jar.bzl",
     "maven_jar",
     "GERRIT",
     "MAVEN_CENTRAL")

JGIT_VERS = "4.7.1.201706071930-r"

JGIT_REPO = MAVEN_CENTRAL

maven_jar(
    name = "jgit_http_apache",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERS,
    sha1 = "16d49a8824753f2d421151c68be05e0869e0b8f6",
    repository = JGIT_REPO,
)

maven_jar(
    name = "jgit_lfs",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs:" + JGIT_VERS,
    sha1 = "35e8245b5c77822581dc354387e8e78846cf4e7e",
    repository = JGIT_REPO,
)

maven_jar(
    name = "jgit_lfs_server",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs.server:" + JGIT_VERS,
    sha1 = "9c4fc91f095b13348081acf40f6c402e10b7255d",
    repository = JGIT_REPO,
)

# Release Plugin API
load("@com_googlesource_gerrit_bazlets//:gerrit_api.bzl",
     "gerrit_api")

# Snapshot Plugin API
#load(
#    "@com_googlesource_gerrit_bazlets//:gerrit_api_maven_local.bzl",
#    "gerrit_api_maven_local",
#)

# Load release Plugin API
gerrit_api()

# Load snapshot Plugin API
#gerrit_api_maven_local()
