# @PLUGIN@ -  REST API

This page describes the REST endpoints that are added by the @PLUGIN@ plugin.

Please also take note of the general information on the
[REST API](../../../Documentation/rest-api.html).

## Get project configuration

_GET /projects/project_name/@PLUGIN@:config-project_

Gets the LFS configuration for a specified project.

```
  GET /projects/myproject/@PLUGIN@:config-project HTTP/1.0
```

As response an [LfsProjectConfigInfo](#lfs-project-config-info) entity is
returned that describes the LFS configuration for the project.

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


## Get global configuration

_GET /projects/All-Projects/@PLUGIN@:config-global_

Gets the global LFS configuration. May only be called on `All-Projects` by users
having the 'Administrate Server' capability.

```
  GET /projects/All-Projects/@PLUGIN@:config-global HTTP/1.0
```

As response an [LfsGlobalConfigInfo](#lfs-global-config-info) entity is returned
that describes the global LFS configuration.

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

## Set global configuration

_PUT /projects/All-Projects/@PLUGIN@:config-global_

Sets the global LFS project configuration. The configuration must be provided in
the request body in an [LfsGlobalConfigInput](#lfs-global-config-input) entity.
If an empty body is sent, all current project settings are removed. May only be
called on `All-Projects` by users having the 'Administrate Server' capability.

```
  PUT /projects/All-Projects/@PLUGIN@:settings HTTP/1.0
  Content-Type: application/json;charset=UTF-8
  {
    "namespaces": {
      "test-project": {
        "enabled": false
      }
    }
  }
```

As response an [LfsSettingsInfo](#lfs-settings-info) entity is returned that
describes the updated global LFS settings.


## JSON Entities

### <a id="lfs-project-config-info"></a>LfsProjectConfigInfo

The `LfsProjectConfigInfo` describes the LFS configuration for a project.

* _enabled_: Whether LFS is enabled for this project. Not set if false.
* _max_object_size_: Maximum LFS object size for this project. Only set when
_enabled_ is true. 0 means no limit is set.

### <a id="lfs-global-config-info"></a>LfsGlobalConfigInfo

The `LfsGlobalConfigInfo` entity describes the global configuration for LFS.

* _backend_: The LFS backend in use. Can be `FS` or `S3`.
* _namespaces_: Configured namespaces as a map of [LfsProjectConfigInfo]
(#lfs-project-config-info) entities.

### <a id="lfs-global-config-input"></a>LfsGlobalConfigInput

The `LfsGlobalConfigInput` entity describes the global settings for LFS.

* _namespaces_: Configured namespaces as a map of [LfsProjectConfigInfo]
(#lfs-project-config-info) entities.
