/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQFEATUREITERATORTIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class _SeqFeatureIteratorTie extends org.biocorba.Bio._SeqFeatureIteratorImplBase {
    public org.biocorba.Bio._SeqFeatureIteratorOperations servant;
    public _SeqFeatureIteratorTie(org.biocorba.Bio._SeqFeatureIteratorOperations servant) {
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
    public org.biocorba.Bio.SeqFeature next()
    {
        return servant.next();
    }
    public boolean has_more()
    {
        return servant.has_more();
    }
}
