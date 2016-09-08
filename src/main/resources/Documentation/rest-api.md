# @PLUGIN@ -  REST API

This page describes the REST endpoints that are added by the @PLUGIN@ plugin.

Please also take note of the general information on the
[REST API](../../../Documentation/rest-api.html).

## Get configuration

_GET /projects/project_name/@PLUGIN@:config_

Gets the LFS configuration for the specified project. When called on
the `All-Projects` project and the caller has the 'Administrate Server'
capability, gets the global LFS configuration.

### Get project configuration

```
  GET /projects/myproject/@PLUGIN@:config HTTP/1.0
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


### Get global configuration

```
  GET /projects/All-Projects/@PLUGIN@:config HTTP/1.0
```

As response an [LfsGlobalConfigInfo](#lfs-global-config-info) entity is
returned that describes the global LFS configuration.

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

### <a id="lfs-project-config-info"></a>LfsProjectConfigInfo

The `LfsProjectConfigInfo` describes the LFS configuration for a project.

* _enabled_: Whether LFS is enabled for this project. Not set if false.
* _max_object_size_: Maximum LFS object size for this project. Only set when
_enabled_ is true. 0 means no limit is set.

### <a id="lfs-global-config-info"></a>LfsGlobalConfigInfo

The `LfsGlobalConfigInfo` entity describes the global configuration
for LFS.

* _backend_: The LFS backend in use. Can be `FS` or `S3`.
* _namespaces_: Configured namespaces as a map of
[LfsProjectConfigInfo](#lfs-project-config-info) entities.
