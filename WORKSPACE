workspace(name = "lfs")

load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "8a4cbdc993f41fcfe7290e7d1007cfedf8d87c18",
    #    local_path = "/home/<user>/projects/bazlets",
)

load("@com_googlesource_gerrit_bazlets//tools:maven_jar.bzl",
     "maven_jar",
     "GERRIT")

JGIT_VERS = "4.6.1.201703071140-r.123-g5094c1a5c"

maven_jar(
    name = "jgit_http_apache",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERS,
    sha1 = "74e128137892aa75543552bcc0f6ec65aa56edff",
    repository = GERRIT,
)

maven_jar(
    name = "jgit_lfs",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs:" + JGIT_VERS,
    sha1 = "9b7939132ebe92c36b470a309982ef1604798a0d",
    repository = GERRIT,
)

maven_jar(
    name = "jgit_lfs_server",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs.server:" + JGIT_VERS,
    sha1 = "f915406042c320d3233bf734eec1856f60f279a8",
    repository = GERRIT,
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
