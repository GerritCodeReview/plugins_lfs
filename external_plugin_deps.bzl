load("//tools/bzl:maven_jar.bzl", "maven_jar", "GERRIT", "MAVEN_LOCAL", "MAVEN_CENTRAL")

JGIT_VERSION = '4.8.0.201706111038-r'
REPO = MAVEN_CENTRAL

def external_plugin_deps():
  maven_jar(
    name = 'jgit_http_apache',
    artifact = 'org.eclipse.jgit:org.eclipse.jgit.http.apache:' + JGIT_VERSION,
    sha1 = '78e55a9537e5f6b6fc196d72ad74009550dd3ed9',
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
    sha1 = '7f9906508cb129022120a998bdfb662748a52a79',
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
    sha1 = 'd0631e2b55aeb41ddad167849f33f53a7eb58726',
    repository = REPO,
    unsign = True,
    exclude = [
      'about.html',
      'plugin.properties',
    ],
  )
