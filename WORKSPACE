workspace(name = "lfs")

load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "8a4cbdc993f41fcfe7290e7d1007cfedf8d87c18",
    #    local_path = "/home/<user>/projects/bazlets",
)

load("@com_googlesource_gerrit_bazlets//tools:maven_jar.bzl",
     "maven_jar",
     "GERRIT")

JGIT_VERS = "4.6.0.201612231935-r.30-gd3148f300"

maven_jar(
    name = "jgit_http_apache",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERS,
    sha1 = "b9806f94d6b548c85a9ef96ef647b0f15b64927a",
    repository = GERRIT,
)

maven_jar(
    name = "jgit_lfs",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs:" + JGIT_VERS,
    sha1 = "55cf48dd41732ded00d66f2f833e3b7346eb5e37",
    repository = GERRIT,
)

maven_jar(
    name = "jgit_lfs_server",
    artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs.server:" + JGIT_VERS,
    sha1 = "4d917afafe7888bba07607bfa7fcb06bb60fe7f1",
    repository = GERRIT,
)

maven_jar(
    name = "httpcore",
    artifact = "org.apache.httpcomponents:httpcore:4.4.1",
    sha1 = "f5aa318bda4c6c8d688c9d00b90681dcd82ce636",
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
