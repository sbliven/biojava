/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQFEATUREOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
/**

 */public interface _SeqFeature_Operations
	extends org.biocorba.GNOME._Unknown_Operations {
     String type(org.omg.CORBA.portable.ObjectImpl seqFeature)
;
     String source(org.omg.CORBA.portable.ObjectImpl seqFeature)
;
     String seq_primary_id(org.omg.CORBA.portable.ObjectImpl seqFeature)
;
     int start(org.omg.CORBA.portable.ObjectImpl seqFeature)
;
     int end(org.omg.CORBA.portable.ObjectImpl seqFeature)
;
     short strand(org.omg.CORBA.portable.ObjectImpl seqFeature)
;
     org.biocorba.Bio.NameValueSet[] qualifiers(org.omg.CORBA.portable.ObjectImpl seqFeature)
;
     boolean has_PrimarySeq(org.omg.CORBA.portable.ObjectImpl seqFeature)
;
     org.biocorba.Bio.PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl seqFeature)
        throws org.biocorba.Bio.UnableToProcess;
}
