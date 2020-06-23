load("//tools/bzl:maven_jar.bzl", "GERRIT", "ECLIPSE", "MAVEN_LOCAL", "maven_jar")

JGIT_VERSION = "5.8.0.202005061305-m2"
REPO = ECLIPSE

def external_plugin_deps():
    maven_jar(
        name = "jgit-http-apache",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERSION,
        sha1 = "e8d4803362d7a523f7e716b30ff2b77868ce91f6",
        repository = REPO,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )

    maven_jar(
        name = "jgit-lfs",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs:" + JGIT_VERSION,
        sha1 = "5d57e3c8e047335abe2406e392c9711646905b06",
        repository = REPO,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )

    maven_jar(
        name = "jgit-lfs-server",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs.server:" + JGIT_VERSION,
        sha1 = "45d5b77931ab0fc39de9d08c1c3d3e7e9f1c5d2c",
        repository = REPO,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )
