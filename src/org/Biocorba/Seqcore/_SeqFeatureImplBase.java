/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/_SEQFEATUREIMPLBASE.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public abstract class _SeqFeatureImplBase extends org.omg.CORBA.DynamicImplementation implements org.Biocorba.Seqcore.SeqFeature {
    // Constructor
    public _SeqFeatureImplBase() {
         super();
    }
    // Type strings for this class and its superclases
    private static final String _type_ids[] = {
        "IDL:org/Biocorba/Seqcore/SeqFeature:1.0",
        "IDL:GNOME/Unknown:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    private static java.util.Dictionary _methods = new java.util.Hashtable();
    static {
      _methods.put("ref", new java.lang.Integer(0));
      _methods.put("unref", new java.lang.Integer(1));
      _methods.put("query_interface", new java.lang.Integer(2));
      _methods.put("type", new java.lang.Integer(3));
      _methods.put("source", new java.lang.Integer(4));
      _methods.put("seq_primary_id", new java.lang.Integer(5));
      _methods.put("start", new java.lang.Integer(6));
      _methods.put("end", new java.lang.Integer(7));
      _methods.put("strand", new java.lang.Integer(8));
      _methods.put("qualifiers", new java.lang.Integer(9));
      _methods.put("PrimarySeq_is_available", new java.lang.Integer(10));
      _methods.put("get_PrimarySeq", new java.lang.Integer(11));
     }
    // DSI Dispatch call
    public void invoke(org.omg.CORBA.ServerRequest r) {
       switch (((java.lang.Integer) _methods.get(r.op_name())).intValue()) {
           case 0: // GNOME.Unknown.ref
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
                            this.ref();
              org.omg.CORBA.Any __return = _orb().create_any();
              __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
              r.result(__return);
              }
              break;
           case 1: // GNOME.Unknown.unref
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
                            this.unref();
              org.omg.CORBA.Any __return = _orb().create_any();
              __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
              r.result(__return);
              }
              break;
           case 2: // GNOME.Unknown.query_interface
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              org.omg.CORBA.Any _repoid = _orb().create_any();
              _repoid.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
              _list.add_value("repoid", _repoid, org.omg.CORBA.ARG_IN.value);
              r.params(_list);
              String repoid;
              repoid = _repoid.extract_string();
              org.omg.CORBA.Object ___result;
                            ___result = this.query_interface(repoid);
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_Object(___result);
              r.result(__result);
              }
              break;
           case 3: // org.Biocorba.Seqcore.SeqFeature.type
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              String ___result;
                            ___result = this.type();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 4: // org.Biocorba.Seqcore.SeqFeature.source
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              String ___result;
                            ___result = this.source();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 5: // org.Biocorba.Seqcore.SeqFeature.seq_primary_id
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              String ___result;
                            ___result = this.seq_primary_id();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 6: // org.Biocorba.Seqcore.SeqFeature.start
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              int ___result;
                            ___result = this.start();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_long(___result);
              r.result(__result);
              }
              break;
           case 7: // org.Biocorba.Seqcore.SeqFeature.end
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              int ___result;
                            ___result = this.end();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_long(___result);
              r.result(__result);
              }
              break;
           case 8: // org.Biocorba.Seqcore.SeqFeature.strand
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              short ___result;
                            ___result = this.strand();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_short(___result);
              r.result(__result);
              }
              break;
           case 9: // org.Biocorba.Seqcore.SeqFeature.qualifiers
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              org.Biocorba.Seqcore.NameValueSet[] ___result;
                            ___result = this.qualifiers();
              org.omg.CORBA.Any __result = _orb().create_any();
              org.Biocorba.Seqcore.NameValueSetListHelper.insert(__result, ___result);
              r.result(__result);
              }
              break;
           case 10: // org.Biocorba.Seqcore.SeqFeature.PrimarySeq_is_available
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              boolean ___result;
                            ___result = this.PrimarySeq_is_available();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_boolean(___result);
              r.result(__result);
              }
              break;
           case 11: // org.Biocorba.Seqcore.SeqFeature.get_PrimarySeq
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              org.Biocorba.Seqcore.PrimarySeq ___result;
              try {
                            ___result = this.get_PrimarySeq();
              }
              catch (org.Biocorba.Seqcore.UnableToProcess e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            org.Biocorba.Seqcore.UnableToProcessHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              org.Biocorba.Seqcore.PrimarySeqHelper.insert(__result, ___result);
              r.result(__result);
              }
              break;
            default:
              throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
       }
 }
}
