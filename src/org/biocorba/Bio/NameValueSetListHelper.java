/*
 * File: SRC/ORG/BIOCORBA/BIO/NAMEVALUESETLISTHELPER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class NameValueSetListHelper {
     // It is useless to have instances of this class
     private NameValueSetListHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, org.biocorba.Bio.NameValueSet[] that)  {
          {
              out.write_long(that.length);
              for (int __index = 0 ; __index < that.length ; __index += 1) {
                  org.biocorba.Bio.NameValueSetHelper.write(out, that[__index]);
              }
          }
    }
    public static org.biocorba.Bio.NameValueSet[] read(org.omg.CORBA.portable.InputStream in) {
          org.biocorba.Bio.NameValueSet[] that;
          {
              int __length = in.read_long();
              that = new org.biocorba.Bio.NameValueSet[__length];
              for (int __index = 0 ; __index < that.length ; __index += 1) {
                  that[__index] = org.biocorba.Bio.NameValueSetHelper.read(in);
              }
          }
          return that;
    }
   public static org.biocorba.Bio.NameValueSet[] extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, org.biocorba.Bio.NameValueSet[] that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     a.type(type());
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_alias_tc(id(), "NameValueSetList", org.omg.CORBA.ORB.init().create_sequence_tc(0, org.biocorba.Bio.NameValueSetHelper.type()));
      return _tc;
   }
   public static String id() {
       return "IDL:Bio/NameValueSetList:1.0";
   }
}
