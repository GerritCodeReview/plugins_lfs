# @PLUGIN@ -  REST API

This page describes the REST endpoints that are added by the @PLUGIN@ plugin.

Please also take note of the general information on the
[REST API](../../../Documentation/rest-api.html).

## Get project configuration

_GET /projects/project_name/@PLUGIN@:config_

Gets the LFS configuration for the specified project.

```
  GET /projects/myproject/@PLUGIN@:config HTTP/1.0
```

As response an [LfsConfigInfo](#lfs-config-info) entity is returned that
describes the LFS configuration for the project.

```
  HTTP/1.1 200 OK
  Content-Disposition: attachment
  Content-Type: application/json;charset=UTF-8
  )]}'
  {
    "enabled": true,
    "max_object_size": 102400
  }
```


## Get global settings

_GET /projects/All-Projects/@PLUGIN@:settings_

Gets the global LFS settings.

```
  GET /projects/All-Projects/@PLUGIN@:settings HTTP/1.0
```

As response an [LfsSettingsInfo](#lfs-settings-info) entity is returned that
describes the global LFS settings. May only be called on `All-Projects` by
users having the 'Administrate Server' capability.

```
  HTTP/1.1 200 OK
  Content-Disposition: attachment
  Content-Type: application/json;charset=UTF-8
  )]}'
  {
    "backend": "FS",
    "namespaces": {
      "test-project": {
        "enabled": true,
        "max_object_size": 102400
      }
    }
  }
```

## JSON Entities

### <a id="lfs-config-info"></a>LfsConfigInfo

The `LfsConfigInfo` describes the LFS configuration for a project.

* _enabled_: Whether LFS is enabled for this project. Not set if false.
* _max_object_size_: Maximum LFS object size for this project. Only set when
_enabled_ is true. 0 means no limit is set.

### <a id="lfs-settings-info"></a>LfsSettingsInfo

The `LfsSettingsInfo` entity describes the global settings for LFS.

* _backend_: The LFS backend in use. Can be `FS` or `S3`.
* _namespaces_: Configured namespaces as a map of [LfsConfigInfo](#lfs-config-info)
entities.
