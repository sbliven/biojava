/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/PRIMARYSEQITERATOR.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public interface PrimarySeqIterator
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    GNOME.Unknown {
    org.Biocorba.Seqcore.PrimarySeq next()
        throws org.Biocorba.Seqcore.EndOfStream, org.Biocorba.Seqcore.UnableToProcess;
    boolean has_more()
;
}
