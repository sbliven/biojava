/*
 * File: SRC/BIO/REQUESTTOOLARGEHELPER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public class RequestTooLargeHelper {
     // It is useless to have instances of this class
     private RequestTooLargeHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, Bio.RequestTooLarge that) {
    out.write_string(id());

	out.write_string(that.reason);
	out.write_long(that.suggested_size);
    }
    public static Bio.RequestTooLarge read(org.omg.CORBA.portable.InputStream in) {
        Bio.RequestTooLarge that = new Bio.RequestTooLarge();
         // read and discard the repository id
        in.read_string();

	that.reason = in.read_string();
	that.suggested_size = in.read_long();
    return that;
    }
   public static Bio.RequestTooLarge extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, Bio.RequestTooLarge that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
       int _memberCount = 2;
       org.omg.CORBA.StructMember[] _members = null;
          if (_tc == null) {
               _members= new org.omg.CORBA.StructMember[2];
               _members[0] = new org.omg.CORBA.StructMember(
                 "reason",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                 null);

               _members[1] = new org.omg.CORBA.StructMember(
                 "suggested_size",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long),
                 null);
             _tc = org.omg.CORBA.ORB.init().create_exception_tc(id(), "RequestTooLarge", _members);
          }
      return _tc;
   }
   public static String id() {
       return "IDL:Bio/RequestTooLarge:1.0";
   }
}
