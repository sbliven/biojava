/*
 * File: SRC/BIO/_PRIMARYSEQIMPLBASE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public abstract class _PrimarySeqImplBase extends org.omg.CORBA.DynamicImplementation implements Bio.PrimarySeq {
    // Constructor
    public _PrimarySeqImplBase() {
         super();
    }
    // Type strings for this class and its superclases
    private static final String _type_ids[] = {
        "IDL:Bio/PrimarySeq:1.0",
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
      _methods.put("display_id", new java.lang.Integer(7));
      _methods.put("primary_id", new java.lang.Integer(8));
      _methods.put("accession_number", new java.lang.Integer(9));
      _methods.put("version", new java.lang.Integer(10));
      _methods.put("max_request_length", new java.lang.Integer(11));
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
           case 3: // Bio.PrimarySeq.type
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              Bio.SeqType ___result;
                            ___result = this.type();
              org.omg.CORBA.Any __result = _orb().create_any();
              Bio.SeqTypeHelper.insert(__result, ___result);
              r.result(__result);
              }
              break;
           case 4: // Bio.PrimarySeq.length
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
           case 5: // Bio.PrimarySeq.get_seq
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              String ___result;
              try {
                            ___result = this.get_seq();
              }
              catch (Bio.RequestTooLarge e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            Bio.RequestTooLargeHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 6: // Bio.PrimarySeq.get_subseq
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
              catch (Bio.OutOfRange e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            Bio.OutOfRangeHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              catch (Bio.RequestTooLarge e1) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            Bio.RequestTooLargeHelper.insert(_except, e1);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 7: // Bio.PrimarySeq.display_id
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              String ___result;
                            ___result = this.display_id();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 8: // Bio.PrimarySeq.primary_id
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              String ___result;
                            ___result = this.primary_id();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 9: // Bio.PrimarySeq.accession_number
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              String ___result;
                            ___result = this.accession_number();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 10: // Bio.PrimarySeq.version
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              int ___result;
                            ___result = this.version();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_long(___result);
              r.result(__result);
              }
              break;
           case 11: // Bio.PrimarySeq.max_request_length
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
