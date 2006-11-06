#! /bin/sh 

home=../
java=/usr/opt/java/bin/java

cp=$home/build/ensj.jar
cp=$cp:$home
cp=$cp:$home/lib/log4j-1.2.6.jar
cp=$cp:$home/lib/java-getopt-1.0.9.jar
cp=$cp:$home/lib/mysql-connector-java-3.0.15-ga-bin.jar
cp=$cp:$home/lib/p6spy.jar
cp=$cp:$home/lib/looks-1.2.1.jar

$java -server -cp $cp -Xmx1500m org.ensembl.idmapping.gui.Main $* 
