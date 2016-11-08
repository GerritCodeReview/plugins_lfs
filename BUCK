include_defs('//bucklets/gerrit_plugin.bucklet')
include_defs('//bucklets/maven_jar.bucklet')

JGIT_VERSION = '4.5.0.201609210915-r'
REPO = MAVEN_CENTRAL

gerrit_plugin(
  name = 'lfs',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  deps = [
    ':jgit-http-apache',
    ':jgit-lfs',
    ':jgit-lfs-server',
    ':httpcore',
  ],
  manifest_entries = [
    'Gerrit-PluginName: lfs',
    'Gerrit-Module: com.googlesource.gerrit.plugins.lfs.Module',
    'Gerrit-HttpModule: com.googlesource.gerrit.plugins.lfs.HttpModule',
  ],
)

# httpcore version should match version used in Gerrit
maven_jar(
  name = 'httpcore',
  id = 'org.apache.httpcomponents:httpcore:4.4.1',
  bin_sha1 = 'f5aa318bda4c6c8d688c9d00b90681dcd82ce636',
  src_sha1 = '9700be0d0a331691654a8e901943c9a74e33c5fc',
  license = 'Apache2.0',
)

maven_jar(
  name = 'jgit-http-apache',
  id = 'org.eclipse.jgit:org.eclipse.jgit.http.apache:' + JGIT_VERSION,
  sha1 = 'ce43489af3eb68740d2c5c67939fc15e1d87e082',
  license = 'jgit',
  repository = REPO,
  unsign = True,
  exclude = [
    'about.html',
    'plugin.properties',
  ],
)

maven_jar(
  name = 'jgit-lfs',
  id = 'org.eclipse.jgit:org.eclipse.jgit.lfs:' + JGIT_VERSION,
  bin_sha1 = 'f6252a849c8dfcf6ea6526b1891986dd7176735c',
  src_sha1 = '62d5694f2db58ecef0b227d6943bf1ae26536e24',
  license = 'jgit',
  repository = REPO,
  unsign = True,
  exclude = [
    'about.html',
    'plugin.properties',
  ],
)

maven_jar(
  name = 'jgit-lfs-server',
  id = 'org.eclipse.jgit:org.eclipse.jgit.lfs.server:' + JGIT_VERSION,
  bin_sha1 = 'cdcc6bcc5e9db699301b776af22f3dab1cba348b',
  src_sha1 = 'fe23815a06a10b11b9ec27e4d244f12c771fc6e4',
  license = 'jgit',
  repository = REPO,
  unsign = True,
  exclude = [
    'about.html',
    'plugin.properties',
  ],
)
