/*
 * File: SRC/BIO/SEQFEATURE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
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
    Bio.NameValueSet[] qualifiers()
;
    boolean has_PrimarySeq()
;
    Bio.PrimarySeq get_PrimarySeq()
        throws Bio.UnableToProcess;
}
