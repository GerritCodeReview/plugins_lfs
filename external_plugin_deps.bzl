load("//tools/bzl:maven_jar.bzl", "GERRIT", "MAVEN_CENTRAL", "MAVEN_LOCAL", "maven_jar")

JGIT_VERSION = "4.7.1.201706071930-r"
REPO = MAVEN_CENTRAL

def external_plugin_deps():
    maven_jar(
        name = "jgit_http_apache",
        artifact = "org.eclipse.jgit:org.eclipse.jgit.http.apache:" + JGIT_VERSION,
        sha1 = "16d49a8824753f2d421151c68be05e0869e0b8f6",
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
        sha1 = "35e8245b5c77822581dc354387e8e78846cf4e7e",
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
        sha1 = "9c4fc91f095b13348081acf40f6c402e10b7255d",
        repository = REPO,
        unsign = True,
        exclude = [
            "about.html",
            "plugin.properties",
        ],
    )
