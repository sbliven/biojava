#! /bin/sh
PACKAGES_CORE=org.biojava.bio.seq:org.biojava.bio.seq.io:org.biojava.bio.seq.tools:org.biojava.bio.dp:org.biojava.bio.gui:org.biojava.bio.program:org.biojava.bio.program.gff
PACKAGES_DEVELOPMENT=org.biojava.stats.svm:org.biojava.stats.svm.tools:org.biojava.utils.xml
PACKAGES_CORBA=GNOME:org.Biocorba.Seqcore:org.biojava.bridge.GNOME:org.biojava.bridge.Biocorba.Seqcore
PACKAGES_ACEDB=org.acedb:org.acedb.seq:org.acedb.staticobj:org.acedb.socket

PACKAGES=`echo ${PACKAGES_CORE} ${PACKAGES_DEVELOPMENT} ${PACKAGES_CORBA} ${PACKAGES_ACEDB} | tr ':' ' '`

mkdir docs
javadoc \
 -sourcepath src \
 -classpath xml.jar:class \
 -d docs \
 -private -use -version -author \
 -windowtitle "Biojava API documentation" \
 -group "Core Packages" ${PACKAGES_CORE} \
 -group "Development Packages" ${PACKAGES_DEVELOPMENT} \
 -group "Corba Packages" ${PACKAGES_CORBA} \
 -group "ACeDB Connection Packages" ${PACKAGES_ACEDB} \
 ${PACKAGES}

cat build/extra.css >> docs/stylesheet.css