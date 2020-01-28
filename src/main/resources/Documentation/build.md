Build
=====

This @PLUGIN@ plugin is built with Bazel.

Two build modes are supported: Standalone and in Gerrit tree.
The standalone build mode is recommended, as this mode doesn't require
the Gerrit tree to exist locally.

### Build standalone

```
  bazel build @PLUGIN@
```

The output is created in

```
  bazel-bin/@PLUGIN@.jar
```

To execute the tests run:

```
  bazel test //...
```

This project can be imported into the Eclipse IDE. Execute:

```
  ./tools/eclipse/project.sh
```

to generate the required files and then import the project.

### Build in Gerrit tree

Put the external dependency Bazel build file into the Gerrit /plugins directory,
replacing the existing empty one.

```
  cd gerrit/plugins
  rm external_plugin_deps.bzl
  ln -s @PLUGIN@/external_plugin_deps.bzl .
```

Then issue

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

This project can be imported into the Eclipse IDE.
Add the plugin name to the `CUSTOM_PLUGINS` set in
Gerrit core in `tools/bzl/plugins.bzl`, and execute:

```
  ./tools/eclipse/project.py
```

How to build the Gerrit Plugin API is described in the [Gerrit
documentation](../../../Documentation/dev-bazel.html#_extension_and_plugin_api_jar_files).

[Back to @PLUGIN@ documentation index][index]

[index]: index.html