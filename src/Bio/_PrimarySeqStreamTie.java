/*
 * File: SRC/BIO/_PRIMARYSEQSTREAMTIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public class _PrimarySeqStreamTie extends Bio._PrimarySeqStreamImplBase {
    public Bio._PrimarySeqStreamOperations servant;
    public _PrimarySeqStreamTie(Bio._PrimarySeqStreamOperations servant) {
           this.servant = servant;
    }
    public void ref()
    {
        servant.ref();
    }
    public void unref()
    {
        servant.unref();
    }
    public org.omg.CORBA.Object query_interface(String repoid)
    {
        return servant.query_interface(repoid);
    }
    public Bio.PrimarySeq next()
        throws Bio.EndOfStream, Bio.UnableToProcess    {
        return servant.next();
    }
    public boolean has_more()
    {
        return servant.has_more();
    }
}
