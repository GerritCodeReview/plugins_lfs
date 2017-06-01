workspace(name = "lfs")

load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "74b31c8fae3a92c6c3e46a046b57cf1d8a6549d4",
    #    local_path = "/home/<user>/projects/bazlets",
)

load("@com_googlesource_gerrit_bazlets//tools:maven_jar.bzl",
     "maven_jar",
     "GERRIT",
     "MAVEN_CENTRAL")

JGIT_VERS = "4.7.0.201704051617-r"

JGIT_REPO = MAVEN_CENTRAL

maven_jar(
    name = "jgit_http_apache",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERS,
    sha1 = "da1fc3c8f6ce89dd846125a38fd4c35f2436caf3",
    repository = JGIT_REPO,
)

maven_jar(
    name = "jgit_lfs",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs:" + JGIT_VERS,
    sha1 = "d71dbf2f97544cc6deff966e6d069b76049958e0",
    repository = JGIT_REPO,
)

maven_jar(
    name = "jgit_lfs_server",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs.server:" + JGIT_VERS,
    sha1 = "edf48b77d97abc70f0e048bd04864132b30d2be5",
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
