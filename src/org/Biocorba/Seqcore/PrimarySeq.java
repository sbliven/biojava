/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/PRIMARYSEQ.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public interface PrimarySeq
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    org.Biocorba.Seqcore.AnonymousSeq {
    String display_id()
;
    String primary_id()
;
    String accession_number()
;
    int version()
;
}
