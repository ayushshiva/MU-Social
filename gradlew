#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or at least 512M, for a 64-bit JVM.
# DEFAULT_JVM_OPTS='"-Xmx512m" "-Xms512m"'

# Warn the user if they're using an older version of Java.
# if [ -n "$JAVA_HOME" ] ; then
#     if [ -x "$JAVA_HOME/bin/java" ] ; then
#         # Use $JAVA_HOME/bin/java -version to check.
#     fi
# fi

# ... (Simplified for brevity, standard gradlew content)
exec "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
