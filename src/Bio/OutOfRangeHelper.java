/*
 * File: SRC/BIO/OUTOFRANGEHELPER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public class OutOfRangeHelper {
     // It is useless to have instances of this class
     private OutOfRangeHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, Bio.OutOfRange that) {
    out.write_string(id());

	out.write_string(that.reason);
    }
    public static Bio.OutOfRange read(org.omg.CORBA.portable.InputStream in) {
        Bio.OutOfRange that = new Bio.OutOfRange();
         // read and discard the repository id
        in.read_string();

	that.reason = in.read_string();
    return that;
    }
   public static Bio.OutOfRange extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, Bio.OutOfRange that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
       int _memberCount = 1;
       org.omg.CORBA.StructMember[] _members = null;
          if (_tc == null) {
               _members= new org.omg.CORBA.StructMember[1];
               _members[0] = new org.omg.CORBA.StructMember(
                 "reason",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                 null);
             _tc = org.omg.CORBA.ORB.init().create_exception_tc(id(), "OutOfRange", _members);
          }
      return _tc;
   }
   public static String id() {
       return "IDL:Bio/OutOfRange:1.0";
   }
}
