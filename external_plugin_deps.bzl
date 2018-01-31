load("//tools/bzl:maven_jar.bzl", "maven_jar", "GERRIT", "MAVEN_LOCAL", "MAVEN_CENTRAL")

JGIT_VERSION = '4.10.0.201712302008-r.24-gf3bb0e268'
REPO = GERRIT

def external_plugin_deps():
  maven_jar(
    name = 'jgit_http_apache',
    artifact = 'org.eclipse.jgit:org.eclipse.jgit.http.apache:' + JGIT_VERSION,
    sha1 = '701c6ec6c9a06311055d7468e4d0be021d62f900',
    repository = REPO,
    unsign = True,
    exclude = [
      'about.html',
      'plugin.properties',
    ],
  )

  maven_jar(
    name = 'jgit_lfs',
    artifact = 'org.eclipse.jgit:org.eclipse.jgit.lfs:' + JGIT_VERSION,
    sha1 = 'f0b47053c2d4a71d20a04176e3e78fadc9705695',
    repository = REPO,
    unsign = True,
    exclude = [
      'about.html',
      'plugin.properties',
    ],
  )

  maven_jar(
    name = 'jgit_lfs_server',
    artifact = 'org.eclipse.jgit:org.eclipse.jgit.lfs.server:' + JGIT_VERSION,
    sha1 = '18472cf8aa256c8d2f7841898e8007d5fd5c7ab4',
    repository = REPO,
    unsign = True,
    exclude = [
      'about.html',
      'plugin.properties',
    ],
  )

  maven_jar(
    name = 'joda_time',
    artifact = 'joda-time:joda-time:2.9.9',
    sha1 = 'f7b520c458572890807d143670c9b24f4de90897',
  )

