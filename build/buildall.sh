#! /usr/sh
find src -name "*.java" | xargs javac -classpath xml.jar:class -sourcepath src -d class
jar -cf biojava.jar -C class .
jar -uf biojava.jar -C resources .
