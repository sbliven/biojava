#! /bin/csh
set PACKAGES_CORE=org.biojava.bio.seq:org.biojava.bio.seq.io:org.biojava.bio.seq.tools:org.biojava.bio.dp:org.biojava.bio.gui:org.biojava.bio.program:org.biojava.bio.program.gff
set PACKAGES_DEVELOPMENT=org.biojava.stats.svm:org.biojava.stats.svm.tools:org.biojava.utils.xml
set PACKAGES_CORBA=GNOME:org.Biocorba.Seqcore:org.biojava.bridge.GNOME:org.biojava.bridge.Biocorba.Seqcore
set PACKAGES_ACEDB=org.acedb:org.acedb.seq:org.acedb.staticobj:org.acedb.socket

set PACKAGES=`echo ${PACKAGES_CORE} ${PACKAGES_DEVELOPMENT} ${PACKAGES_CORBA} ${PACKAGES_ACEDB} | tr ':' ' '`

rm -rf docs
mkdir docs
mkdir docs/api
javadoc \
 -sourcepath src \
 -classpath xml.jar:class \
 -d docs/api \
 -private -use -version -author \
 -windowtitle "Biojava API documentation" \
 -group "Core Packages" ${PACKAGES_CORE} \
 -group "Development Packages" ${PACKAGES_DEVELOPMENT} \
 -group "Corba Packages" ${PACKAGES_CORBA} \
 -group "ACeDB Connection Packages" ${PACKAGES_ACEDB} \
 ${PACKAGES}

cat build/extra.css >> docs/api/stylesheet.css

mkdir docs/demos
set DEMOS=`find demos/* -type d -prune | grep -v 'CVS' | xargs echo`
foreach DEMO (${DEMOS})
  echo Found demo $DEMO
  set CLASSES=`ls ${DEMO}/*.java`
  mkdir docs/$DEMO
  javadoc \
    -sourcepath $DEMO \
    -classpath xml.jar:class \
    -d docs/$DEMO \
    -author \
    -windowtitle "$DEMO" \
    -link docs/api \
    -nohelp \
    -notree \
    -nodeprecatedlist \
    -nodeprecated \
    $CLASSES
  cat build/extra.css >> docs/${DEMO}/stylesheet.css
end
