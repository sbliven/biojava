/*
 * File: SRC/BIO/SEQFEATURELISTHELPER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public class SeqFeatureListHelper {
     // It is useless to have instances of this class
     private SeqFeatureListHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, Bio.SeqFeature[] that)  {
          {
              out.write_long(that.length);
              for (int __index = 0 ; __index < that.length ; __index += 1) {
                  Bio.SeqFeatureHelper.write(out, that[__index]);
              }
          }
    }
    public static Bio.SeqFeature[] read(org.omg.CORBA.portable.InputStream in) {
          Bio.SeqFeature[] that;
          {
              int __length = in.read_long();
              that = new Bio.SeqFeature[__length];
              for (int __index = 0 ; __index < that.length ; __index += 1) {
                  that[__index] = Bio.SeqFeatureHelper.read(in);
              }
          }
          return that;
    }
   public static Bio.SeqFeature[] extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, Bio.SeqFeature[] that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     a.type(type());
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_alias_tc(id(), "SeqFeatureList", org.omg.CORBA.ORB.init().create_sequence_tc(0, Bio.SeqFeatureHelper.type()));
      return _tc;
   }
   public static String id() {
       return "IDL:Bio/SeqFeatureList:1.0";
   }
}
