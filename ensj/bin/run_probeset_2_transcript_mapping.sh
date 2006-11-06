#!/bin/sh

# Convenience script for running the ProbeMapper java program.

# USAGE: 
#   Run with "-h" parameter to see usage.

# Dependencies:
#  - java is on PATH
#  - if running on Tru64 computer there should be at least 3GB memory 
#    (the program will run else where and with less memory
#    if all the data will fit into memory)

# Tip: Run script via nohup so that process continues to run after you log out.
#    nohup run_probeset_2_transcript.sh > nohup.txt 2>&1 &

# The program maps microarray probesets to transcripts and stores the
# results as xrefs in the database.

# This script automatically builds the java command and saves the user
# having to worry about the classpath and fully qualifying the name of
# the java program.

ROOT=`dirname $0`/..
CP="${ROOT}/build/classes"
#CP="${CP}:"`ls ${ROOT}/build/ensj-*.jar`
CP="${CP}:${ROOT}/lib/mysql-connector-java-3.1.8-bin.jar"
CP="${CP}:${ROOT}/lib/java-getopt-1.0.9.jar"
CP="${CP}:${ROOT}/lib/p6spy.jar"
CP="${CP}:${ROOT}/lib/colt.jar"
CP="${CP}:${ROOT}/src"

OPTIONS="-Xmx1000m -Xprof"
PLATFORM=`uname -ms`
case "$PLATFORM" in
*alpha*)
  # Tru64 JVM switches
  OPTIONS="-fast64 -Xmx3000m -Dskip_xref_check"
  ;;
esac

JAVA=/usr/opt/java/bin/java
if [ ! -e "$JAVA" ]
then
  JAVA="java"
fi
CMD="${JAVA} ${OPTIONS}  -classpath ${CP} org.ensembl.probemapping.ProbeMapper $@"

echo ${CMD}
echo
$CMD




