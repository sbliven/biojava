/*
 * File: SRC/BIO/_SEQDBOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
/**

 */public interface _SeqDBOperations
	extends Bio._PrimarySeqDBOperations {
     Bio.Seq get_Seq(String primary_id)
        throws Bio.UnableToProcess;
}
