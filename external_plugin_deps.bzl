load("//tools/bzl:maven_jar.bzl", "GERRIT", "MAVEN_CENTRAL", "MAVEN_LOCAL", "maven_jar")

JGIT_VERSION = "5.8.0.202006091008-r"
REPO = MAVEN_CENTRAL

def external_plugin_deps():
    maven_jar(
        name = "jgit-http-apache",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERSION,
        sha1 = "bc241ab161163dc47e02f81e1bf6751f4bd2e164",
        repository = REPO,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )

    maven_jar(
        name = "jgit-lfs",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs:" + JGIT_VERSION,
        sha1 = "96ba6bf319cfc19262720fce3a0c96d746bb392e",
        repository = REPO,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )

    maven_jar(
        name = "jgit-lfs-server",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs.server:" + JGIT_VERSION,
        sha1 = "9f1fa2e6a44147032de81c69c57da9be3d2078c9",
        repository = REPO,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )

    maven_jar(
        name = "joda-time",
        artifact = "joda-time:joda-time:2.9.9",
        sha1 = "f7b520c458572890807d143670c9b24f4de90897",
    )
