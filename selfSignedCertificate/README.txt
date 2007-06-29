
deskpro349[ap3]100: pwd
/nfs/team71/phd/ap3/workspace/biojava-live
deskpro349[ap3]101: cd selfSignedCertificate/
deskpro349[ap3]102: keytool -v -genkey -keyalg RSA -validity 10000 -keystore selfSignedCertificate.store -alias biojavaCVS
Enter keystore password:
Re-enter new password:
What is your first and last name?
  [Unknown]:  BioJava www.biojava.org
What is the name of your organizational unit?
  [Unknown]:  BioJava developers
What is the name of your organization?
  [Unknown]:  BioJava open source project
What is the name of your City or Locality?
  [Unknown]:  www.biojava.org
What is the name of your State or Province?
  [Unknown]:  www.biojava.org
What is the two-letter country code for this unit?
  [Unknown]:
Is CN=BioJava www.biojava.org, OU=BioJava developers, O=BioJava open source project, L=www.biojava.org, ST=www.biojava.org, C=Unknown correct?
  [no]:  yes

Generating 1,024 bit RSA key pair and self-signed certificate (SHA1withRSA) with a validity of 10,000 days
        for: CN=BioJava www.biojava.org, OU=BioJava developers, O=BioJava open source project, L=www.biojava.org, ST=www.biojava.org, C=Unknown
Enter key password for <biojavaCVS>
        (RETURN if same as keystore password):
[Storing selfSignedCertificate.store]
deskpro349[ap3]103: keytool -export -keystore selfSignedCertificate.store -alias biojavaCVS -file selfSignedCertificate.cer
Enter keystore password:
Certificate stored in file <selfSignedCertificate.cer>
deskpro349[ap3]104: cd ..
deskpro349[ap3]105: jarsigner -verbose -keystore selfSignedCertificate/selfSignedCertificate.store -storepass biojavaCVS -signedjar ant-build/biojavaSigned.jar ant-build/biojava.jar biojavaCVS 

...cut here...

deskpro349[ap3]106: jarsigner -verify ant-build/biojavaSigned.jar
jar verified.


