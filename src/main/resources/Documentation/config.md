# Plugin @PLUGIN@ configuration

## Core Gerrit Settings

The following option must be set in `$GERRIT_SITE/etc/gerrit.config`.

### Section `lfs`

lfs.plugin = @PLUGIN@
: With this option set LFS requests are forwarded to the @PLUGIN@ plugin.

## Per Project Settings

The following options can be configured in `@PLUGIN@.config` on the
`refs/meta/config` branch of the `All-Projects` project.

```
  [@PLUGIN@ "sandbox/*"]
    enabled = true
    maxObjectSize = 10m
  [@PLUGIN@ "public/*"]
    enabled = true
    maxObjectSize = 200m
  [@PLUGIN@ "customerX/*"]
    enabled = true
    maxObjectSize = 500m
  [@PLUGIN@ "customerY/*"]
    enabled = false
```

A namespace can be specified as

- exact project name (`plugins/myPlugin`): Defines LFS settings for one project.

- pattern (`sandbox/*`): Defines LFS settings for one project namespace.

- regular expression (`^test-.*/.*`): Defines LFS settings for the namespace
matching the regular expression.

- for-each-pattern (`?/*`): Defines the same LFS settings for each subfolder.
`?` is a placeholder for any name and `?/*` with `maxObjectSize = 100m` means
that for every subfolder the maximum object size is `100 mb`. Hence `?/*` is a
shortcut for having n explicit namespaces.

If a project name matches several @PLUGIN@ namespaces, the one the that is defined
first in the @PLUGIN@.config will be applied.

Example: Enable LFS for all projects, allowing unlimited object size for
projected under `/test` and limiting to `500 mb` for projects under other
folders:

```
  [@PLUGIN@ "test/*]
    enabled = true
  [@PLUGIN@ "?/*"]
    enabled = true
    maxObjectSize = 500m
```

Example: Only enable LFS for projects under `customer-A`:

```
  [@PLUGIN@ "customer-A/*]
    enabled = true
```


### Section `lfs`

lfs.enabled
: Whether to enable LFS for projects in this namespace. If not set, defaults
to `false`.

lfs.maxObjectSize
: Maximum allowed object size (per object) in bytes for projects in this
namespace, or 0 for no limit. If not set, defaults to 0. Common unit suffixes
of `k`, `m`, and `g` are supported.

lfs.readOnly
: Whether to switch LFS for projects in this namespace into read-only mode.
In this mode reading LFS objects is still possible but pushing is forbidden.
Note that regular git operations are not affected.
If not set, defaults to `false`.

## Global Plugin Settings

The following options can be configured in `$GERRIT_SITE/etc/@PLUGIN@.config`
and `$GERRIT_SITE/etc/@PLUGIN@.secure.config.`

### Section `storage`

storage.backend
: The storage backend to use. Valid values are `fs` for local file system,
and `s3` for Amazon S3. If not set, defaults to `fs`.

### Section `fs`

The following configuration options are only used when the backend is `fs`.

fs.directory
: The directory in which to store data files. If not specified, defaults to
the plugin's data folder: `$GERRIT_SITE/data/@PLUGIN@`.

### Section `s3`

The following configuration options are only used when the backend is `s3`.

s3.region
: link:http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html#concepts-available-regions
[Amazon region] the S3 storage bucket is residing in.

s3.bucket
: Name of the link:http://docs.aws.amazon.com/AmazonS3/latest/UG/CreatingaBucket.html
[Amazon S3 storage bucket] which will store large objects.

s3.storageClass
: link:http://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html
[Amazon S3 storage class] used for storing large objects.
: Default is `REDUCED_REDUNDANCY`

s3.expiration
: Expiration in seconds of validity of signed requests. Gerrit's LFS protocol
handler signs requests to be issued by the git-lfs extension with the configured
`accessKey` and `secretKey`. This way the git-lfs extension doesn't need
any credentials to access objects in the S3 bucket. Validity of these request
signatures expires after this period.
: Default is `60` seconds.

s3.disableSslVerify
: `true`: SSL verification is disabled
: `false`: SSL verification is enabled
: Default is `false`.

s3.accessKey
: The link:http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html
[Amazon IAM accessKey] for authenticating to S3. It is recommended to place this
setting in `$GERRIT_SITE/etc/@PLUGIN@.secure.config`.

s3.secretKey
: The link:http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html
[Amazon IAM secretKey] for authenticating to S3. It is recommended to place this
setting in `$GERRIT_SITE/etc/@PLUGIN@.secure.config`.

## Local Project Configuration

The following options must be set in the local project's `.git/config` file.

### Section `lfs`

lfs.url
: `http://<username>@<gerrit-host>:<port>/<project-name>/info/lfs`

When the Gerrit repo is cloned via ssh, the git lfs url must be set to use http.
