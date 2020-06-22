load("//tools/bzl:maven_jar.bzl", "GERRIT", "MAVEN_CENTRAL", "MAVEN_LOCAL", "maven_jar")

JGIT_VERSION = "5.5.1.201910021850-r"
REPO = MAVEN_CENTRAL

def external_plugin_deps(use_lfs_from_gerrit_tree = True):
    if not use_lfs_from_gerrit_tree:
        maven_jar(
            name = "jgit-http-apache",
            artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERSION,
            sha1 = "aaf5367ff2cba4174b4165ed0a64dc92cac75871",
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
            sha1 = "c37af6b2d28df21942e935753922333c8612cef2",
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
            sha1 = "7df786dfb9843a01e46cbf48ef921c5aac087ca1",
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
