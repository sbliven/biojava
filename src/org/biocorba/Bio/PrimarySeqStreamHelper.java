/*
 * File: SRC/ORG/BIOCORBA/BIO/PRIMARYSEQSTREAMHELPER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class PrimarySeqStreamHelper {
     // It is useless to have instances of this class
     private PrimarySeqStreamHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, org.biocorba.Bio.PrimarySeqStream that) {
        out.write_Object(that);
    }
    public static org.biocorba.Bio.PrimarySeqStream read(org.omg.CORBA.portable.InputStream in) {
        return org.biocorba.Bio.PrimarySeqStreamHelper.narrow(in.read_Object());
    }
   public static org.biocorba.Bio.PrimarySeqStream extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, org.biocorba.Bio.PrimarySeqStream that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_interface_tc(id(), "PrimarySeqStream");
      return _tc;
   }
   public static String id() {
       return "IDL:Bio/PrimarySeqStream:1.0";
   }
   public static org.biocorba.Bio.PrimarySeqStream narrow(org.omg.CORBA.Object that)
	    throws org.omg.CORBA.BAD_PARAM {
        if (that == null)
            return null;
        if (that instanceof org.biocorba.Bio.PrimarySeqStream)
            return (org.biocorba.Bio.PrimarySeqStream) that;
	if (!that._is_a(id())) {
	    throw new org.omg.CORBA.BAD_PARAM();
	}
        org.omg.CORBA.portable.Delegate dup = ((org.omg.CORBA.portable.ObjectImpl)that)._get_delegate();
        org.biocorba.Bio.PrimarySeqStream result = new org.biocorba.Bio._PrimarySeqStreamStub(dup);
        return result;
   }
}
