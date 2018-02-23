load("//tools/bzl:maven_jar.bzl", "maven_jar", "GERRIT", "MAVEN_LOCAL", "MAVEN_CENTRAL")

JGIT_VERSION = '4.9.2.201712150930-r.4-g085d1f959'
REPO = GERRIT

def external_plugin_deps():
  lfs()

def lfs():
  maven_jar(
    name = 'jgit_http_apache',
    artifact = 'org.eclipse.jgit:org.eclipse.jgit.http.apache:' + JGIT_VERSION,
    sha1 = '13859b30c8a20eb99dca3b9d2bb595e82d90320b',
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
    sha1 = '70dea6582f956fbaea003391fac8d79d280a9d24',
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
    sha1 = '1e704bf986e2f882666ca3b8ae1446137346b8ee',
    repository = REPO,
    unsign = True,
    exclude = [
      'about.html',
      'plugin.properties',
    ],
  )
