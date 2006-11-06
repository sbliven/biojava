#! /bin/sh 

# Usage example - Run via farm using custom idmapping.properties file in current working directory:
# 
#    lsrun -P -R linux ~/dev/ensj-core/bin/run_idmapping.sh `pwd`/idmapping.properties

# note that the script attempts to use ensj classes from:
# 1 - build/classes
# 2 - build/ensj.jar 

home=`dirname $0`/..
java=/usr/opt/java/bin/java

cp=$home/src
cp=$cp:$home/build/classes
cp=$cp:$home/build/ensj.jar
cp=$cp:$home/lib/log4j-1.2.6.jar
cp=$cp:$home/lib/java-getopt-1.0.9.jar
cp=$cp:$home/lib/mysql-connector-java-3.1.8-bin.jar
cp=$cp:$home/lib/p6spy.jar
cp=$cp:$home/lib/colt.jar

$java -ea -server -cp $cp -Xmx1700m org.ensembl.idmapping.IDMappingApplication $* 
