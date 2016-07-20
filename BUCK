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
  sha1 = '8b29be1bf313d3708e80a615b9632ea3b9a85dd7',
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
  bin_sha1 = 'ab16d86901bb1a9fd0b36032c846837a53b3c459',
  src_sha1 = '875504a7fb4dd34458b549331064eada2848ef66',
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
  bin_sha1 = '9e46ef66f4e48d11b4846a893f3528175025a159',
  src_sha1 = '103c0650465dc45fdeddfa37145be6a7f3fd1d94',
  license = 'jgit',
  repository = REPO,
  unsign = True,
  exclude = [
    'about.html',
    'plugin.properties',
  ],
)
