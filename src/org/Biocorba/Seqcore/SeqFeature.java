/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/SEQFEATURE.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public interface SeqFeature
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    GNOME.Unknown {
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
    org.Biocorba.Seqcore.NameValueSet[] qualifiers()
;
    boolean PrimarySeq_is_available()
;
    org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq()
        throws org.Biocorba.Seqcore.UnableToProcess;
}
