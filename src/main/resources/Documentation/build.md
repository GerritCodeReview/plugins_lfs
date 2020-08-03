Build
=====

This @PLUGIN@ plugin is built with Bazel.

Link the plugin directory in plugins directory in Gerrit core and run

```
  bazel build plugins/@PLUGIN@
```

The output is created in

```
  bazel-bin/plugins/@PLUGIN@/@PLUGIN@.jar
```

To execute the tests run either one of:

```
  bazel test --test_tag_filters=@PLUGIN@ //...
  bazel test plugins/@PLUGIN@:@PLUGIN@_tests
```

This project can be imported into the Eclipse IDE in the following steps:
1. Add the plugin name to the `CUSTOM_PLUGINS` set in Gerrit core in
  `tools/bzl/plugins.bzl`.

1. Add sources for `jgit.http.apache`, `jgit.lfs` and `jgit.lfs.server` JGit
  libraries set in `import_jgit_sources` method of Gerrit core
  `tools/eclipse/project.py`:

  ```
  classpathentry('src', 'org.eclipse.jgit.http.apache/src')
  classpathentry('src', 'org.eclipse.jgit.http.apache/resources')
  classpathentry('src', 'modules/jgit/org.eclipse.jgit.lfs/src')
  classpathentry('src', 'modules/jgit/org.eclipse.jgit.lfs/resources')
  classpathentry('src', 'modules/jgit/org.eclipse.jgit.lfs.server/src')
  classpathentry('src', 'modules/jgit/org.eclipse.jgit.lfs.server/resources')
  ```

1. Finally execute:

  ```
  ./tools/eclipse/project.py
  ```

How to build the Gerrit Plugin API is described in the [Gerrit
documentation](../../../Documentation/dev-bazel.html#_extension_and_plugin_api_jar_files).

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
