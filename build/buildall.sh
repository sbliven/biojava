#! /bin/sh
rm -rf class biojava.jar
mkdir class
find src -name "*.java" | xargs javac -classpath xml.jar:class -sourcepath src -d class
jar -cf biojava.jar -C class .
jar -uf biojava.jar -C resources .
jar -ufm biojava.jar manifest/defaultmanifest.txt

