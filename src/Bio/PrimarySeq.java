/*
 * File: SRC/BIO/PRIMARYSEQ.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public interface PrimarySeq
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    GNOME.Unknown {
    Bio.SeqType type()
;
    int length()
;
    String get_seq()
        throws Bio.RequestTooLarge;
    String get_subseq(int start, int end)
        throws Bio.OutOfRange, Bio.RequestTooLarge;
    String display_id()
;
    String primary_id()
;
    String accession_number()
;
    int version()
;
    int max_request_length()
;
}
