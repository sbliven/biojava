/*
 * File: SRC/BIO/_PRIMARYSEQTIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public class _PrimarySeqTie extends Bio._PrimarySeqImplBase {
    public Bio._PrimarySeqOperations servant;
    public _PrimarySeqTie(Bio._PrimarySeqOperations servant) {
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
    public Bio.SeqType type()
    {
        return servant.type();
    }
    public int length()
    {
        return servant.length();
    }
    public String get_seq()
        throws Bio.RequestTooLarge    {
        return servant.get_seq();
    }
    public String get_subseq(int start, int end)
        throws Bio.OutOfRange, Bio.RequestTooLarge    {
        return servant.get_subseq(start, end);
    }
    public String display_id()
    {
        return servant.display_id();
    }
    public String primary_id()
    {
        return servant.primary_id();
    }
    public String accession_number()
    {
        return servant.accession_number();
    }
    public int version()
    {
        return servant.version();
    }
    public int max_request_length()
    {
        return servant.max_request_length();
    }
}
