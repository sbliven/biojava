/*
 * File: SRC/BIO/PRIMARYSEQDB.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public interface PrimarySeqDB
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    GNOME.Unknown {
    String database_name()
;
    short database_version()
;
    Bio.PrimarySeqStream make_stream()
;
    Bio.PrimarySeq get_PrimarySeq(String primary_id)
        throws Bio.UnableToProcess;
}
