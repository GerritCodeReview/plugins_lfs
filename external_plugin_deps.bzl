load("//tools/bzl:maven_jar.bzl", "GERRIT", "MAVEN_CENTRAL", "MAVEN_LOCAL", "maven_jar")

JGIT_VERSION = "4.9.6.201810051924-r"
REPO = MAVEN_CENTRAL

def external_plugin_deps():
    maven_jar(
        name = "jgit_http_apache",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERSION,
        sha1 = "c6f4ed2fc4ce6f6c4b0a0dfa54bb4ab7dbbee557",
        repository = REPO,
        unsign = True,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )

    maven_jar(
        name = "jgit_lfs",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs:" + JGIT_VERSION,
        sha1 = "b716ddad524bf9b8f75475e232d803345df21423",
        repository = REPO,
        unsign = True,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )

    maven_jar(
        name = "jgit_lfs_server",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.lfs.server:" + JGIT_VERSION,
        sha1 = "2d69c6ca1349c67415a9371c6ff103e90565c9de",
        repository = REPO,
        unsign = True,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )
