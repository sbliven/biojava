/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/PRIMARYSEQDB.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public interface PrimarySeqDB
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    GNOME.Unknown {
    String database_name()
;
    short database_version()
;
    org.Biocorba.Seqcore.PrimarySeqIterator make_PrimarySeqIterator()
;
    org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq(String primary_id)
        throws org.Biocorba.Seqcore.UnableToProcess;
}
