/*
 * File: SRC/BIO/PRIMARYSEQSTREAM.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public interface PrimarySeqStream
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    GNOME.Unknown {
    Bio.PrimarySeq next()
        throws Bio.EndOfStream, Bio.UnableToProcess;
    boolean has_more()
;
}
