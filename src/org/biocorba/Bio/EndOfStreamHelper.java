/*
 * File: SRC/ORG/BIOCORBA/BIO/ENDOFSTREAMHELPER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class EndOfStreamHelper {
     // It is useless to have instances of this class
     private EndOfStreamHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, org.biocorba.Bio.EndOfStream that) {
    out.write_string(id());
 }
    public static org.biocorba.Bio.EndOfStream read(org.omg.CORBA.portable.InputStream in) {
        org.biocorba.Bio.EndOfStream that = new org.biocorba.Bio.EndOfStream();
         // read and discard the repository id
        in.read_string();
    return that;
    }
   public static org.biocorba.Bio.EndOfStream extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, org.biocorba.Bio.EndOfStream that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
       int _memberCount = 0;
       org.omg.CORBA.StructMember[] _members = null;
          if (_tc == null) {
               _members= new org.omg.CORBA.StructMember[0];
             _tc = org.omg.CORBA.ORB.init().create_exception_tc(id(), "EndOfStream", _members);
          }
      return _tc;
   }
   public static String id() {
       return "IDL:Bio/EndOfStream:1.0";
   }
}
