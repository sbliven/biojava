/*
 * File: SRC/ORG/BIOCORBA/BIO/PRIMARYSEQSTREAM.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public interface PrimarySeqStream
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    org.biocorba.GNOME.Unknown {
    org.biocorba.Bio.PrimarySeq next()
        throws org.biocorba.Bio.EndOfStream, org.biocorba.Bio.UnableToProcess;
    boolean has_more()
;
}
