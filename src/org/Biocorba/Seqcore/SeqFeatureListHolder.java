/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/SEQFEATURELISTHOLDER.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public final class SeqFeatureListHolder
    implements org.omg.CORBA.portable.Streamable
{
    //	instance variable 
    public org.Biocorba.Seqcore.SeqFeature[] value;
    //	constructors 
    public SeqFeatureListHolder() {
	this(null);
    }
    public SeqFeatureListHolder(org.Biocorba.Seqcore.SeqFeature[] __arg) {
	value = __arg;
    }
    public void _write(org.omg.CORBA.portable.OutputStream out) {
        org.Biocorba.Seqcore.SeqFeatureListHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = org.Biocorba.Seqcore.SeqFeatureListHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.Biocorba.Seqcore.SeqFeatureListHelper.type();
    }
}
