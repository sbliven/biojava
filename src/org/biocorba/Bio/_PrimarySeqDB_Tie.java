/*
 * File: SRC/ORG/BIOCORBA/BIO/_PRIMARYSEQDBTIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class _PrimarySeqDB_Tie extends org.biocorba.Bio._PrimarySeqDBImplBase {
    public org.biocorba.Bio._PrimarySeqDB_Operations servant;
    public _PrimarySeqDB_Tie(org.biocorba.Bio._PrimarySeqDB_Operations servant) {
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
    public String database_name()
    {
        return servant.database_name(this);
    }
    public short database_version()
    {
        return servant.database_version(this);
    }
    public org.biocorba.Bio.PrimarySeqStream make_stream()
    {
        return servant.make_stream(this);
    }
    public org.biocorba.Bio.PrimarySeq get_PrimarySeq(String primary_id)
        throws org.biocorba.Bio.UnableToProcess    {
        return servant.get_PrimarySeq(this, primary_id);
    }
}
