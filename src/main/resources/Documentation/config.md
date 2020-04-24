# Plugin @PLUGIN@ configuration

## Core Gerrit Settings

The following options must be set in `$GERRIT_SITE/etc/gerrit.config`.

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
projects under `/test` and limiting to `500 mb` for projects under other
folders:

```
  [@PLUGIN@ "test/*"]
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

lfs.backend
: Backend that should be used for storing binaries. It has to be one of
backends specified as [fs](#lfs-fs-backend) or [s3](#lfs-s3-backend) subsection
of Global Plugin Settings. If not set, defaults to value of `storage.backend`
from Global Plugin Settings.

## Global Plugin Settings

The following options can be configured in `$GERRIT_SITE/etc/@PLUGIN@.config`
and `$GERRIT_SITE/etc/@PLUGIN@.secure.config.`

### Section `locks`

The [Git LFS File Locking API](https://github.com/git-lfs/git-lfs/blob/master/docs/api/locking.md)
specifies that a certain path can be locked by a user. It prevents the file
being accidentally overwritten by a different user, and costly (manual in
most cases) binary file merge. Each lock is represented by a JSON structure:

```
 {
    "id":"[lock id the same as lock file name]",
    "path":"[path to the resource being locked]",
    "locked_at":"[timestamp the lock was created in ISO 8601 format]",
    "owner":{
      "name":"[the name of the user that created the lock]"
    }
  }
```

The lock is stored in a file whose name is the SHA256 hash of the path being
locked, under `locks.directory` followed by the project name.

locks.directory
: The directory in which to store Git LFS file locks.

: Default is `$GERRIT_SITE/data/@PLUGIN@/lfs_locks`.

### Section `auth`

auth.sshExpirationSeconds
: Validity, in seconds, of authentication token for SSH requests.
[Git LFS Authentication](https://github.com/git-lfs/git-lfs/blob/master/docs/api/authentication.md)
specifies that SSH might be used to authenticate the user. Successful authentication
provides token that is later used for Git LFS requests.
: Default is `10` seconds.

### Section `storage`

storage.backend
: The default storage backend to use. Valid values are `fs` for local file system,
and `s3` for Amazon S3. If not set, defaults to `fs`.

### <a id="lfs-fs-backend"></a>Section `fs` - default file system backend

The following configuration options are only used when the backend is `fs`.

fs.directory
: The directory in which to store data files. If not specified, defaults to
the plugin's data folder: `$GERRIT_SITE/data/@PLUGIN@`.

fs.expirationSeconds
: Validity, in seconds, of authentication token for signed requests.
Gerrit's LFS protocol handler signs requests to be issued by the git-lfs
extension. This way the git-lfs extension doesn't need any credentials to
access objects in the FS bucket. Validity of these request signatures expires
after this period.
: Default is `10` seconds.

### <a id="lfs-s3-backend"></a>Section `s3` - default S3 backend

The following configuration options are only used when the backend is `s3`.

s3.hostname
: Custom hostname for the S3 API server. This will allow for easier local testing
of gerrit instances with lfs storage and for the utilization of custom storage
solutions. If not specified, lfs will use AWS as the backend.

s3.region
: [Amazon region](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html#concepts-available-regions)
the S3 storage bucket is residing in.

s3.bucket
: Name of the [Amazon S3 storage bucket](http://docs.aws.amazon.com/AmazonS3/latest/UG/CreatingaBucket.html)
 which will store large objects.

s3.storageClass
: [Amazon S3 storage class](http://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html)
 used for storing large objects.
: Default is `REDUCED_REDUNDANCY`

s3.expirationSeconds
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
: The [Amazon IAM accessKey](http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html)
for authenticating to S3. It is recommended to place this
setting in `$GERRIT_SITE/etc/@PLUGIN@.secure.config`.

s3.secretKey
: The [Amazon IAM secretKey](http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html)
 for authenticating to S3. It is recommended to place this
setting in `$GERRIT_SITE/etc/@PLUGIN@.secure.config`.

### Multiple LFS backends

One can specify multiple LFS backends for both FS and S3 storage by introducing
backend subsections:

```
  [fs "foo"]
    directory = /foo_dir
  [s3 "bar"]
    ...
```

and use them for namespace configuration by adding backend namespace parameter:

```
  [@PLUGIN@ "sandbox/*"]
    backend = foo
    ...
  [@PLUGIN@ "release/*"]
    backend = bar
    ...
```

## Local Project Configuration

The following options must be set in the local project's `.git/config` file.

### Section `lfs`

lfs.url
: `http://<username>@<gerrit-host>:<port>/<project-name>/info/lfs`

When the Gerrit repo is cloned via ssh, the git lfs url must be set to use http.
