/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQTIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class _SeqTie extends org.biocorba.Bio._SeqImplBase {
    public org.biocorba.Bio._SeqOperations servant;
    public _SeqTie(org.biocorba.Bio._SeqOperations servant) {
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
    public org.biocorba.Bio.SeqType type()
    {
        return servant.type();
    }
    public int length()
    {
        return servant.length();
    }
    public String get_seq()
        throws org.biocorba.Bio.RequestTooLarge    {
        return servant.get_seq();
    }
    public String get_subseq(int start, int end)
        throws org.biocorba.Bio.OutOfRange, org.biocorba.Bio.RequestTooLarge    {
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
    public org.biocorba.Bio.SeqFeature[] all_features()
        throws org.biocorba.Bio.RequestTooLarge    {
        return servant.all_features();
    }
    public org.biocorba.Bio.SeqFeatureIterator all_features_iterator()
    {
        return servant.all_features_iterator();
    }
    public org.biocorba.Bio.SeqFeature[] features_region(int start, int end)
        throws org.biocorba.Bio.OutOfRange, org.biocorba.Bio.UnableToProcess, org.biocorba.Bio.RequestTooLarge    {
        return servant.features_region(start, end);
    }
    public org.biocorba.Bio.SeqFeatureIterator features_region_iterator(int start, int end)
        throws org.biocorba.Bio.OutOfRange, org.biocorba.Bio.UnableToProcess    {
        return servant.features_region_iterator(start, end);
    }
    public int max_feature_request()
    {
        return servant.max_feature_request();
    }
    public org.biocorba.Bio.PrimarySeq get_PrimarySeq()
    {
        return servant.get_PrimarySeq();
    }
}
