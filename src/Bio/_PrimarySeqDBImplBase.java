/*
 * File: SRC/BIO/_PRIMARYSEQDBIMPLBASE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public abstract class _PrimarySeqDBImplBase extends org.omg.CORBA.DynamicImplementation implements Bio.PrimarySeqDB {
    // Constructor
    public _PrimarySeqDBImplBase() {
         super();
    }
    // Type strings for this class and its superclases
    private static final String _type_ids[] = {
        "IDL:Bio/PrimarySeqDB:1.0",
        "IDL:GNOME/Unknown:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    private static java.util.Dictionary _methods = new java.util.Hashtable();
    static {
      _methods.put("ref", new java.lang.Integer(0));
      _methods.put("unref", new java.lang.Integer(1));
      _methods.put("query_interface", new java.lang.Integer(2));
      _methods.put("database_name", new java.lang.Integer(3));
      _methods.put("database_version", new java.lang.Integer(4));
      _methods.put("make_stream", new java.lang.Integer(5));
      _methods.put("get_PrimarySeq", new java.lang.Integer(6));
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
           case 3: // Bio.PrimarySeqDB.database_name
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              String ___result;
                            ___result = this.database_name();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 4: // Bio.PrimarySeqDB.database_version
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              short ___result;
                            ___result = this.database_version();
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_short(___result);
              r.result(__result);
              }
              break;
           case 5: // Bio.PrimarySeqDB.make_stream
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              Bio.PrimarySeqStream ___result;
                            ___result = this.make_stream();
              org.omg.CORBA.Any __result = _orb().create_any();
              Bio.PrimarySeqStreamHelper.insert(__result, ___result);
              r.result(__result);
              }
              break;
           case 6: // Bio.PrimarySeqDB.get_PrimarySeq
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              org.omg.CORBA.Any _primary_id = _orb().create_any();
              _primary_id.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
              _list.add_value("primary_id", _primary_id, org.omg.CORBA.ARG_IN.value);
              r.params(_list);
              String primary_id;
              primary_id = _primary_id.extract_string();
              Bio.PrimarySeq ___result;
              try {
                            ___result = this.get_PrimarySeq(primary_id);
              }
              catch (Bio.UnableToProcess e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            Bio.UnableToProcessHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              Bio.PrimarySeqHelper.insert(__result, ___result);
              r.result(__result);
              }
              break;
            default:
              throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
       }
 }
}
