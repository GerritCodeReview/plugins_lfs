include_defs('//lib/maven.defs')
include_defs('//lib/JGIT_VERSION')

gerrit_plugin(
  name = 'lfs',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  deps = [
    ':jgit-http-apache',
    ':jgit-lfs',
    ':jgit-lfs-server',
  ],
  manifest_entries = [
    'Gerrit-PluginName: lfs',
    'Gerrit-HttpModule: com.googlesource.gerrit.plugins.lfs.HttpModule',
  ],
)

maven_jar(
  name = 'jgit-http-apache',
  id = 'org.eclipse.jgit:org.eclipse.jgit.http.apache:' + VERS,
  sha1 = '3e72674ab2ec6f7768e65c13dd0d78c69ee2fb3c',
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
  id = 'org.eclipse.jgit:org.eclipse.jgit.lfs:' + VERS,
  bin_sha1 = '0020fb993f3e361303d53a8f78630e30c3f43e99',
  src_sha1 = '4dce0c10b9f48d147233031617866b6aa36b8c70',
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
  id = 'org.eclipse.jgit:org.eclipse.jgit.lfs.server:' + VERS,
  bin_sha1 = '4a0d74a94aaa367c77325a646fc263d1d5bbdcbf',
  src_sha1 = '5cec3e0f6b6c3e9cde1ab2ee978e287bd6d27218',
  license = 'jgit',
  repository = REPO,
  unsign = True,
  exclude = [
    'about.html',
    'plugin.properties',
  ],
)
