#! /bin/csh -f
set PACKAGES=(org.biojava.bio.gui \
org.biojava.bio.seq \
org.biojava.bio.seq.io \
org.biojava.bio.seq.tools \
org.biojava.bio.acedb \
org.biojava.bio.acedb.seq \
org.biojava.bio.acedb.staticobj \
org.biojava.bio.acedb.socket \
org.biojava.bio.alignment \
org.biojava.bio.program \
org.biojava.stats.svm \
org.biojava.stats.svm.tools \
org.biojava.utils.xml)

echo $PACKAGES
mkdir docs
javadoc \
 -sourcepath src \
 -classpath xml.jar:class \
 -d docs \
 -private -use -version -author \
 $PACKAGES
