# Plugin @PLUGIN@ configuration

## Core Gerrit Settings

The following option must be set in `$GERRIT_SITE/etc/gerrit.config`.

### Section `lfs`

lfs.plugin = @PLUGIN@
: With this option set LFS requests are forwarded to the @PLUGIN@ plugin.

## Per Project Settings

The following options can be configured in `project.config` on the
`refs/meta/config` branch of any project. The plugin provides menu options
to configure it from the project `General` tab. It allows inheritance of
settings as well for maintaining a flexible configuration hierarchy

lfs.backend
: Backend that should be used for storing binaries. It has to be one of
backends specified as [fs](#lfs-fs-backend) or [s3](#lfs-s3-backend) subsection
of Global Plugin Settings. If not set, the LFS backend is disabled.

lfs.maxObjectSize
: Maximum allowed object size (per object) in bytes for projects in this
namespace, or 0 for no limit. If not set, defaults to 0. Common unit suffixes
of `k`, `m`, and `g` are supported.

lfs.writable
: Whether to switch LFS for projects in this namespace into read-only mode.
If set to `false` in this mode, reading LFS objects is still possible but
pushing is forbidden. Note that regular git operations are not affected.
If not set, defaults to `true`.

## Global Plugin Settings

The following options can be configured in `$GERRIT_SITE/etc/@PLUGIN@.config`
and `$GERRIT_SITE/etc/@PLUGIN@.secure.config.`

### Section `url`

url.download
: (Optional) The download HTTP url to be used for LFS traffic
If not set, the CanonicalWebUrl in $GERRIT_SITE/etc/gerrit.config will be used.

url.upload
: (Optional) The upload HTTP url to be used for LFS traffic
If not set, the CanonicalWebUrl in $GERRIT_SITE/etc/gerrit.config will be used.

Usecase for these settings can be the following:
: Install this plugin on both master and slave servers, and redirect the download
or upload traffic to another server. It can also be used to offload the master
server completely for LFS traffic.

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
NOTE: All namespaces must be unique across all LFS backend types. Name clashes
between for example fs and s3 are not allowed.

## Local Project Configuration

The following options must be set in the local project's `.git/config` file.

### Section `lfs`

lfs.url
: `http://<username>@<gerrit-host>:<port>/<project-name>/info/lfs`

When the Gerrit repo is cloned via ssh, the git lfs url must be set to use http.
