/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQFEATURETIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class _SeqFeature_Tie extends org.biocorba.Bio._SeqFeatureImplBase {
    public org.biocorba.Bio._SeqFeature_Operations servant;
    public _SeqFeature_Tie(org.biocorba.Bio._SeqFeature_Operations servant) {
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
    public String type()
    {
        return servant.type(this);
    }
    public String source()
    {
        return servant.source(this);
    }
    public String seq_primary_id()
    {
        return servant.seq_primary_id(this);
    }
    public int start()
    {
        return servant.start(this);
    }
    public int end()
    {
        return servant.end(this);
    }
    public short strand()
    {
        return servant.strand(this);
    }
    public org.biocorba.Bio.NameValueSet[] qualifiers()
    {
        return servant.qualifiers(this);
    }
    public boolean has_PrimarySeq()
    {
        return servant.has_PrimarySeq(this);
    }
    public org.biocorba.Bio.PrimarySeq get_PrimarySeq()
        throws org.biocorba.Bio.UnableToProcess    {
        return servant.get_PrimarySeq(this);
    }
}
