package=`echo $1 | tr '.' '/'`
ls src/${package}/*.java | xargs javac -classpath xml.jar:class -sourcepath src -d class
jar -uf biojava.jar -C class ${package}
