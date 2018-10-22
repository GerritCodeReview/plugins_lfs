load("//tools/bzl:maven_jar.bzl", "GERRIT", "MAVEN_CENTRAL", "MAVEN_LOCAL", "maven_jar")

JGIT_VERSION = "4.7.6.201810191618-r"
REPO = MAVEN_CENTRAL

def external_plugin_deps():
    maven_jar(
        name = "jgit-http-apache",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERSION,
        sha1 = "7da2e0837c13b74aa1cb8705c1f00c8a1763d30e",
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
        sha1 = "c42a483d39dad65e3e87854da385433b4ad45ead",
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
        sha1 = "ff24f0d81c5e5f98389221a83671586581e95b31",
        repository = REPO,
        unsign = True,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )
