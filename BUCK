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
  sha1 = 'ef0c8e787e7622ef9a67ddf3fdae106f46bfa741',
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
  bin_sha1 = '26e592494f7ef0e2132d9432a6527ae16a6bff95',
  src_sha1 = 'bebc30c40935ded6dde1e36d448f99f027587f1e',
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
  bin_sha1 = '3cb6976810231bc13315e3fb380ced815897ceba',
  src_sha1 = 'fa26fee0f83fd72a8771c0fbd1cfe94352dc43e1',
  license = 'jgit',
  repository = REPO,
  unsign = True,
  exclude = [
    'about.html',
    'plugin.properties',
  ],
)
