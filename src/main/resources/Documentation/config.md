@PLUGIN@ Configuration
======================

The following options can be configured in `etc/gerrit.config`.

[[plugin.@PLUGIN@]]
== Section plugin.@PLUGIN@
[[plugin.@PLUGIN@.backend]]plugin.@PLUGIN@.backend::
+
The storage backend to use. Valid values are `FS` for local file system,
and `S3` for Amazon S3. If not set, defaults to `FS`.

[[FS]]
=== Local filesystem configuration

The following configuration options are only used when the backend is `FS`.

[[plugin.@PLUGIN@.directory]]plugin.@PLUGIN@.directory::
+
The directory in which to store data files. If not specified, defaults to
the plugin's data folder: `$GERRIT_SITE/data/@PLUGIN@`.

[[S3]]
=== Amazon S3 configuration

The following configuration options are only used when the backend is `S3`.

[[plugin.@PLUGIN@.region]]plugin.@PLUGIN@.region::
+
link:http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html#concepts-available-regions
[Amazon region] the S3 storage bucket is residing in.

[[plugin.@PLUGIN@.bucket]]plugin.@PLUGIN@.bucket::
+
Name of the link:http://docs.aws.amazon.com/AmazonS3/latest/UG/CreatingaBucket.html
[Amazon S3 storage bucket] which will store large objects.

[[plugin.@PLUGIN@.storageClass]]plugin.@PLUGIN@.storageClass::
+
link:http://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html
[Amazon S3 storage class] used for storing large objects.
+
Default is `REDUCED_REDUNDANCY`

[[plugin.@PLUGIN@.expiration]]plugin.@PLUGIN@.expiration::
+
Expiration in seconds of validity of signed requests. Gerrit's LFS protocol
handler signs requests to be issued by the git-lfs extension with the configured
`accessKey` and `secretKey`. This way the git-lfs extension doesn't need
any credentials to access objects in the S3 bucket. Validity of these request
signatures expires after this period.
+
Default is `60` seconds.

[[plugin.@PLUGIN@.disableSslVerify]]plugin.@PLUGIN@.disableSslVerify::
+
`true`: SSL verification is disabled
+
`false`: SSL verification is enabled
+
Default is `false`.

----
The following options can be configured in `etc/secure.config`

[[plugin.@PLUGIN@.accessKey]]plugin.@PLUGIN@.accessKey::
+
The link:http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html
[Amazon IAM accessKey] for authenticating to S3.

[[plugin.@PLUGIN@.secretKey]]plugin.@PLUGIN@.secretKey::
+
The link:http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html
[Amazon IAM secretKey] for authenticating to S3.
