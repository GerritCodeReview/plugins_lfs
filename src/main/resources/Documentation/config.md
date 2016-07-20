Plugin @PLUGIN@ configuration
======================

**The following options can be configured in `$GERRIT_SITE/etc/@PLUGIN@.config`.**

#### Section `storage`


storage.backend
: The storage backend to use. Valid values are `fs` for local file system,
and `s3` for Amazon S3. If not set, defaults to `fs`.


**Local filesystem configuration**

#### Section `fs`

The following configuration options are only used when the backend is `fs`.

fs.directory
: The directory in which to store data files. If not specified, defaults to
the plugin's data folder: `$GERRIT_SITE/data/@PLUGIN@`.

**Amazon S3 configuration**

#### Section `s3`

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

#### Amazon S3 backend configuration in `$GERRIT_SITE/etc/secure.config`

plugin.@PLUGIN@.s3AccessKey
: The link:http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html
[Amazon IAM accessKey] for authenticating to S3.

plugin.@PLUGIN@.s3SecretKey
: The link:http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html
[Amazon IAM secretKey] for authenticating to S3.
