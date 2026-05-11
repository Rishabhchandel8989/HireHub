#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF)
# Maven Wrapper Script
# ----------------------------------------------------------------------------
set -e

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(dirname "$0")}"
exec "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/mvnw" "$@"
