/*
 * File: SRC/ORG/BIOCORBA/GNOME/UNKNOWNHELPER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.GNOME;
public class UnknownHelper {
     // It is useless to have instances of this class
     private UnknownHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, org.biocorba.GNOME.Unknown that) {
        out.write_Object(that);
    }
    public static org.biocorba.GNOME.Unknown read(org.omg.CORBA.portable.InputStream in) {
        return org.biocorba.GNOME.UnknownHelper.narrow(in.read_Object());
    }
   public static org.biocorba.GNOME.Unknown extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, org.biocorba.GNOME.Unknown that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_interface_tc(id(), "Unknown");
      return _tc;
   }
   public static String id() {
       return "IDL:GNOME/Unknown:1.0";
   }
   public static org.biocorba.GNOME.Unknown narrow(org.omg.CORBA.Object that)
	    throws org.omg.CORBA.BAD_PARAM {
        if (that == null)
            return null;
        if (that instanceof org.biocorba.GNOME.Unknown)
            return (org.biocorba.GNOME.Unknown) that;
	if (!that._is_a(id())) {
	    throw new org.omg.CORBA.BAD_PARAM();
	}
        org.omg.CORBA.portable.Delegate dup = ((org.omg.CORBA.portable.ObjectImpl)that)._get_delegate();
        org.biocorba.GNOME.Unknown result = new org.biocorba.GNOME._UnknownStub(dup);
        return result;
   }
}
