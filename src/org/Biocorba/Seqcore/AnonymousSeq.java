/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/ANONYMOUSSEQ.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public interface AnonymousSeq
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    GNOME.Unknown {
    org.Biocorba.Seqcore.SeqType type()
;
    int length()
;
    String get_seq()
        throws org.Biocorba.Seqcore.RequestTooLarge;
    String get_subseq(int start, int end)
        throws org.Biocorba.Seqcore.OutOfRange, org.Biocorba.Seqcore.RequestTooLarge;
    int max_request_length()
;
}
