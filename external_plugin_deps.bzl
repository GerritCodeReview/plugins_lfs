load("//tools/bzl:maven_jar.bzl", "maven_jar", "GERRIT", "MAVEN_LOCAL")

JGIT_VERSION = '4.6.0.201612231935-r.124-g1cac3e615'
REPO = MAVEN_LOCAL

def external_plugin_deps():
  maven_jar(
    name = 'jgit_http_apache',
    artifact = 'org.eclipse.jgit:org.eclipse.jgit.http.apache:' + JGIT_VERSION,
    sha1 = '1cf9a6d0e0a3145b5d3d7c932f984b230b3d4ae9',
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
    sha1 = '5848fcf541a2367f1d19c1f1d97b1dc50381e4a1',
    #src_sha1 = 'fce0bceaece4c7885ffcd0d9405a524799b40db1',
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
    sha1 = '7692eee437f25e7c94dac84929c3cd231fe63715',
    #src_sha1 = '1a31427d3a2940c661a16f51d1b2f96b37511fc6',
    repository = REPO,
    unsign = True,
    exclude = [
      'about.html',
      'plugin.properties',
    ],
  )
