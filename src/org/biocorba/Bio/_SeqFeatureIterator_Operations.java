/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQFEATUREITERATOROPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
/**

 */public interface _SeqFeatureIterator_Operations
	extends org.biocorba.GNOME._Unknown_Operations {
     org.biocorba.Bio.SeqFeature next(org.omg.CORBA.portable.ObjectImpl seqFeatureIterator)
;
     boolean has_more(org.omg.CORBA.portable.ObjectImpl seqFeatureIterator)
;
}
