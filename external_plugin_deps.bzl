load("//tools/bzl:maven_jar.bzl", "ECLIPSE", "GERRIT", "MAVEN_LOCAL", "maven_jar")

JGIT_VERSION = "5.8.0.202005061305-m2"
REPO = ECLIPSE

def external_plugin_deps(use_lfs_from_gerrit_tree = True):
    if not use_lfs_from_gerrit_tree:
        maven_jar(
            name = "jgit-http-apache",
            artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERSION,
            sha1 = "e8d4803362d7a523f7e716b30ff2b77868ce91f6",
            repository = REPO,
            unsign = True,
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
            unsign = True,
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
            unsign = True,
            exclude = [
                "about.html",
                "plugin.properties",
            ],
        )

    maven_jar(
        name = "joda-time",
        testonly = True,
        artifact = "joda-time:joda-time:2.9.9",
        sha1 = "f7b520c458572890807d143670c9b24f4de90897",
    )
