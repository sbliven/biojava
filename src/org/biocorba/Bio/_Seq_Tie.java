/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQTIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class _Seq_Tie extends org.biocorba.Bio._SeqImplBase {
    public org.biocorba.Bio._Seq_Operations servant;
    public _Seq_Tie(org.biocorba.Bio._Seq_Operations servant) {
           this.servant = servant;
    }
    public void ref()
    {
        servant.ref(this);
    }
    public void unref()
    {
        servant.unref(this);
    }
    public org.omg.CORBA.Object query_interface(String repoid)
    {
        return servant.query_interface(this, repoid);
    }
    public org.biocorba.Bio.SeqType type()
    {
        return servant.type(this);
    }
    public int length()
    {
        return servant.length(this);
    }
    public String get_seq()
        throws org.biocorba.Bio.RequestTooLarge    {
        return servant.get_seq(this);
    }
    public String get_subseq(int start, int end)
        throws org.biocorba.Bio.OutOfRange, org.biocorba.Bio.RequestTooLarge    {
        return servant.get_subseq(this, start, end);
    }
    public String display_id()
    {
        return servant.display_id(this);
    }
    public String primary_id()
    {
        return servant.primary_id(this);
    }
    public String accession_number()
    {
        return servant.accession_number(this);
    }
    public int version()
    {
        return servant.version(this);
    }
    public int max_request_length()
    {
        return servant.max_request_length(this);
    }
    public org.biocorba.Bio.SeqFeature[] all_features()
        throws org.biocorba.Bio.RequestTooLarge    {
        return servant.all_features(this);
    }
    public org.biocorba.Bio.SeqFeatureIterator all_features_iterator()
    {
        return servant.all_features_iterator(this);
    }
    public org.biocorba.Bio.SeqFeature[] features_region(int start, int end)
        throws org.biocorba.Bio.OutOfRange, org.biocorba.Bio.UnableToProcess, org.biocorba.Bio.RequestTooLarge    {
        return servant.features_region(this, start, end);
    }
    public org.biocorba.Bio.SeqFeatureIterator features_region_iterator(int start, int end)
        throws org.biocorba.Bio.OutOfRange, org.biocorba.Bio.UnableToProcess    {
        return servant.features_region_iterator(this, start, end);
    }
    public int max_feature_request()
    {
        return servant.max_feature_request(this);
    }
    public org.biocorba.Bio.PrimarySeq get_PrimarySeq()
    {
        return servant.get_PrimarySeq(this);
    }
}
