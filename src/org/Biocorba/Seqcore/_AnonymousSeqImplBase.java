/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/_ANONYMOUSSEQIMPLBASE.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public abstract class _AnonymousSeqImplBase extends org.omg.CORBA.DynamicImplementation implements org.Biocorba.Seqcore.AnonymousSeq {
    // Constructor
    public _AnonymousSeqImplBase() {
         super();
    }
    // Type strings for this class and its superclases
    private static final String _type_ids[] = {
        "IDL:org/Biocorba/Seqcore/AnonymousSeq:1.0",
        "IDL:GNOME/Unknown:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    private static java.util.Dictionary _methods = new java.util.Hashtable();
    static {
      _methods.put("ref", new java.lang.Integer(0));
      _methods.put("unref", new java.lang.Integer(1));
      _methods.put("query_interface", new java.lang.Integer(2));
      _methods.put("type", new java.lang.Integer(3));
      _methods.put("length", new java.lang.Integer(4));
      _methods.put("get_seq", new java.lang.Integer(5));
      _methods.put("get_subseq", new java.lang.Integer(6));
      _methods.put("max_request_length", new java.lang.Integer(7));
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
           case 3: // org.Biocorba.Seqcore.AnonymousSeq.type
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              org.Biocorba.Seqcore.SeqType ___result;
                            ___result = this.type();
              org.omg.CORBA.Any __result = _orb().create_any();
              org.Biocorba.Seqcore.SeqTypeHelper.insert(__result, ___result);
              r.result(__result);
              }
              break;
           case 4: // org.Biocorba.Seqcore.AnonymousSeq.length
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              int ___result;
                            ___result = this.length();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_long(___result);
              r.result(__result);
              }
              break;
           case 5: // org.Biocorba.Seqcore.AnonymousSeq.get_seq
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              String ___result;
              try {
                            ___result = this.get_seq();
              }
              catch (org.Biocorba.Seqcore.RequestTooLarge e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            org.Biocorba.Seqcore.RequestTooLargeHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 6: // org.Biocorba.Seqcore.AnonymousSeq.get_subseq
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              org.omg.CORBA.Any _start = _orb().create_any();
              _start.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
              _list.add_value("start", _start, org.omg.CORBA.ARG_IN.value);
              org.omg.CORBA.Any _end = _orb().create_any();
              _end.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
              _list.add_value("end", _end, org.omg.CORBA.ARG_IN.value);
              r.params(_list);
              int start;
              start = _start.extract_long();
              int end;
              end = _end.extract_long();
              String ___result;
              try {
                            ___result = this.get_subseq(start, end);
              }
              catch (org.Biocorba.Seqcore.OutOfRange e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            org.Biocorba.Seqcore.OutOfRangeHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              catch (org.Biocorba.Seqcore.RequestTooLarge e1) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            org.Biocorba.Seqcore.RequestTooLargeHelper.insert(_except, e1);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 7: // org.Biocorba.Seqcore.AnonymousSeq.max_request_length
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              int ___result;
                            ___result = this.max_request_length();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_long(___result);
              r.result(__result);
              }
              break;
            default:
              throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
       }
 }
}
