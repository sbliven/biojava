/*
 * File: SRC/BIO/SEQDB.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public interface SeqDB
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    Bio.PrimarySeqDB {
    Bio.Seq get_Seq(String primary_id)
        throws Bio.UnableToProcess;
}
