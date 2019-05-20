load(
    "@com_googlesource_gerrit_bazlets//tools:maven_jar.bzl",
    _gerrit = "GERRIT",
    _maven_central = "MAVEN_CENTRAL",
    _maven_jar = "maven_jar",
    _maven_local = "MAVEN_LOCAL",
)

GERRIT = _gerrit
MAVEN_CENTRAL = _maven_central
MAVEN_LOCAL = _maven_local
maven_jar = _maven_jar
