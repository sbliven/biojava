/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/SEQDBHELPER.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public class SeqDBHelper {
     // It is useless to have instances of this class
     private SeqDBHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, org.Biocorba.Seqcore.SeqDB that) {
        out.write_Object(that);
    }
    public static org.Biocorba.Seqcore.SeqDB read(org.omg.CORBA.portable.InputStream in) {
        return org.Biocorba.Seqcore.SeqDBHelper.narrow(in.read_Object());
    }
   public static org.Biocorba.Seqcore.SeqDB extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, org.Biocorba.Seqcore.SeqDB that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_interface_tc(id(), "SeqDB");
      return _tc;
   }
   public static String id() {
       return "IDL:org/Biocorba/Seqcore/SeqDB:1.0";
   }
   public static org.Biocorba.Seqcore.SeqDB narrow(org.omg.CORBA.Object that)
	    throws org.omg.CORBA.BAD_PARAM {
        if (that == null)
            return null;
        if (that instanceof org.Biocorba.Seqcore.SeqDB)
            return (org.Biocorba.Seqcore.SeqDB) that;
	if (!that._is_a(id())) {
	    throw new org.omg.CORBA.BAD_PARAM();
	}
        org.omg.CORBA.portable.Delegate dup = ((org.omg.CORBA.portable.ObjectImpl)that)._get_delegate();
        org.Biocorba.Seqcore.SeqDB result = new org.Biocorba.Seqcore._SeqDBStub(dup);
        return result;
   }
}
