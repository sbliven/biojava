package=`echo $1 | tr '.' '/'`
find src/${package} -name "*.java" | xargs javac -classpath xml.jar:class -sourcepath src -d class
jar -uf biojava.jar -C class ${package}
