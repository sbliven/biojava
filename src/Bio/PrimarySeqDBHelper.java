/*
 * File: SRC/BIO/PRIMARYSEQDBHELPER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public class PrimarySeqDBHelper {
     // It is useless to have instances of this class
     private PrimarySeqDBHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, Bio.PrimarySeqDB that) {
        out.write_Object(that);
    }
    public static Bio.PrimarySeqDB read(org.omg.CORBA.portable.InputStream in) {
        return Bio.PrimarySeqDBHelper.narrow(in.read_Object());
    }
   public static Bio.PrimarySeqDB extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, Bio.PrimarySeqDB that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_interface_tc(id(), "PrimarySeqDB");
      return _tc;
   }
   public static String id() {
       return "IDL:Bio/PrimarySeqDB:1.0";
   }
   public static Bio.PrimarySeqDB narrow(org.omg.CORBA.Object that)
	    throws org.omg.CORBA.BAD_PARAM {
        if (that == null)
            return null;
        if (that instanceof Bio.PrimarySeqDB)
            return (Bio.PrimarySeqDB) that;
	if (!that._is_a(id())) {
	    throw new org.omg.CORBA.BAD_PARAM();
	}
        org.omg.CORBA.portable.Delegate dup = ((org.omg.CORBA.portable.ObjectImpl)that)._get_delegate();
        Bio.PrimarySeqDB result = new Bio._PrimarySeqDBStub(dup);
        return result;
   }
}
