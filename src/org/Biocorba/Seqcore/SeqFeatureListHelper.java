/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/SEQFEATURELISTHELPER.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public class SeqFeatureListHelper {
     // It is useless to have instances of this class
     private SeqFeatureListHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, org.Biocorba.Seqcore.SeqFeature[] that)  {
          {
              out.write_long(that.length);
              for (int __index = 0 ; __index < that.length ; __index += 1) {
                  org.Biocorba.Seqcore.SeqFeatureHelper.write(out, that[__index]);
              }
          }
    }
    public static org.Biocorba.Seqcore.SeqFeature[] read(org.omg.CORBA.portable.InputStream in) {
          org.Biocorba.Seqcore.SeqFeature[] that;
          {
              int __length = in.read_long();
              that = new org.Biocorba.Seqcore.SeqFeature[__length];
              for (int __index = 0 ; __index < that.length ; __index += 1) {
                  that[__index] = org.Biocorba.Seqcore.SeqFeatureHelper.read(in);
              }
          }
          return that;
    }
   public static org.Biocorba.Seqcore.SeqFeature[] extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, org.Biocorba.Seqcore.SeqFeature[] that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     a.type(type());
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_alias_tc(id(), "SeqFeatureList", org.omg.CORBA.ORB.init().create_sequence_tc(0, org.Biocorba.Seqcore.SeqFeatureHelper.type()));
      return _tc;
   }
   public static String id() {
       return "IDL:org/Biocorba/Seqcore/SeqFeatureList:1.0";
   }
}
