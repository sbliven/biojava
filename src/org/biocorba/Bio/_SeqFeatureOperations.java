/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQFEATUREOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
/**

 */public interface _SeqFeatureOperations
	extends org.biocorba.GNOME._UnknownOperations {
     String type()
;
     String source()
;
     String seq_primary_id()
;
     int start()
;
     int end()
;
     short strand()
;
     org.biocorba.Bio.NameValueSet[] qualifiers()
;
     boolean has_PrimarySeq()
;
     org.biocorba.Bio.PrimarySeq get_PrimarySeq()
        throws org.biocorba.Bio.UnableToProcess;
}
