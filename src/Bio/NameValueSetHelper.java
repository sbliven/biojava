/*
 * File: SRC/BIO/NAMEVALUESETHELPER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public class NameValueSetHelper {
     // It is useless to have instances of this class
     private NameValueSetHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, Bio.NameValueSet that) {
	out.write_string(that.name);
	{
	    out.write_long(that.values.length);
	    for (int __index = 0 ; __index < that.values.length ; __index += 1) {
	        out.write_string(that.values[__index]);
	    }
	}
    }
    public static Bio.NameValueSet read(org.omg.CORBA.portable.InputStream in) {
        Bio.NameValueSet that = new Bio.NameValueSet();
	that.name = in.read_string();
	{
	    int __length = in.read_long();
	    that.values = new String[__length];
	    for (int __index = 0 ; __index < that.values.length ; __index += 1) {
	        that.values[__index] = in.read_string();
	    }
	}
        return that;
    }
   public static Bio.NameValueSet extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, Bio.NameValueSet that) {
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
                 "name",
                 org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string),
                 null);

               _members[1] = new org.omg.CORBA.StructMember(
                 "values",
                 org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string)),
                 null);
             _tc = org.omg.CORBA.ORB.init().create_struct_tc(id(), "NameValueSet", _members);
          }
      return _tc;
   }
   public static String id() {
       return "IDL:Bio/NameValueSet:1.0";
   }
}
