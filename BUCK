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
  sha1 = '28a6ec9c293b3a428abb0778b822e504eba2e121',
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
  bin_sha1 = '4499000f4da8f94a3d271171c1d55a4967ee9d2b',
  src_sha1 = '6a5e6e76a17620cca1f8dedfe21ef90cc044f474',
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
  bin_sha1 = 'ad22d0db4797f5c66a434fc5aabd1210fca21c34',
  src_sha1 = '304c7e131191a3f0c3d8da7db04e42d8c979a096',
  license = 'jgit',
  repository = REPO,
  unsign = True,
  exclude = [
    'about.html',
    'plugin.properties',
  ],
)
