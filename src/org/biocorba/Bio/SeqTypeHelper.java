/*
 * File: SRC/ORG/BIOCORBA/BIO/SEQTYPEHELPER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class SeqTypeHelper {
     // It is useless to have instances of this class
     private SeqTypeHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, org.biocorba.Bio.SeqType that)  {
    out.write_long(that.value());
    }
    public static org.biocorba.Bio.SeqType read(org.omg.CORBA.portable.InputStream in)  {
    return org.biocorba.Bio.SeqType.from_int(in.read_long());
    }
   public static org.biocorba.Bio.SeqType extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, org.biocorba.Bio.SeqType that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   private static final int _memberCount = 3;
   private static String[] _members = {
                  "PROTEIN",
                  "DNA",
                  "RNA"
   };
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_enum_tc(id(), "SeqType", _members);
      return _tc;
   }
   public static String id() {
       return "IDL:Bio/SeqType:1.0";
   }
}
