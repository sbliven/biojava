/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/_SEQDBSTUB.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public class _SeqDBStub
	extends org.omg.CORBA.portable.ObjectImpl
    	implements org.Biocorba.Seqcore.SeqDB {

    public _SeqDBStub(org.omg.CORBA.portable.Delegate d) {
          super();
          _set_delegate(d);
    }

    private static final String _type_ids[] = {
        "IDL:org/Biocorba/Seqcore/SeqDB:1.0",
        "IDL:org/Biocorba/Seqcore/PrimarySeqDB:1.0",
        "IDL:GNOME/Unknown:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    //	IDL operations
    //	    Implementation of ::GNOME::Unknown::ref
    public void ref()
 {
           org.omg.CORBA.Request r = _request("ref");
           r.invoke();
   }
    //	    Implementation of ::GNOME::Unknown::unref
    public void unref()
 {
           org.omg.CORBA.Request r = _request("unref");
           r.invoke();
   }
    //	    Implementation of ::GNOME::Unknown::query_interface
    public org.omg.CORBA.Object query_interface(String repoid)
 {
           org.omg.CORBA.Request r = _request("query_interface");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_objref));
           org.omg.CORBA.Any _repoid = r.add_in_arg();
           _repoid.insert_string(repoid);
           r.invoke();
           org.omg.CORBA.Object __result;
           __result = r.return_value().extract_Object();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::PrimarySeqDB::database_name
    public String database_name()
 {
           org.omg.CORBA.Request r = _request("database_name");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::PrimarySeqDB::database_version
    public short database_version()
 {
           org.omg.CORBA.Request r = _request("database_version");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
           r.invoke();
           short __result;
           __result = r.return_value().extract_short();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::PrimarySeqDB::make_PrimarySeqIterator
    public org.Biocorba.Seqcore.PrimarySeqIterator make_PrimarySeqIterator()
 {
           org.omg.CORBA.Request r = _request("make_PrimarySeqIterator");
           r.set_return_type(org.Biocorba.Seqcore.PrimarySeqIteratorHelper.type());
           r.invoke();
           org.Biocorba.Seqcore.PrimarySeqIterator __result;
           __result = org.Biocorba.Seqcore.PrimarySeqIteratorHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::PrimarySeqDB::get_PrimarySeq
    public org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq(String primary_id)
        throws org.Biocorba.Seqcore.UnableToProcess {
           org.omg.CORBA.Request r = _request("get_PrimarySeq");
           r.set_return_type(org.Biocorba.Seqcore.PrimarySeqHelper.type());
           org.omg.CORBA.Any _primary_id = r.add_in_arg();
           _primary_id.insert_string(primary_id);
           r.exceptions().add(org.Biocorba.Seqcore.UnableToProcessHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(org.Biocorba.Seqcore.UnableToProcessHelper.type())) {
                   throw org.Biocorba.Seqcore.UnableToProcessHelper.extract(__userEx.except);
               }
           }
           org.Biocorba.Seqcore.PrimarySeq __result;
           __result = org.Biocorba.Seqcore.PrimarySeqHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqDB::get_Seq
    public org.Biocorba.Seqcore.Seq get_Seq(String primary_id)
        throws org.Biocorba.Seqcore.UnableToProcess {
           org.omg.CORBA.Request r = _request("get_Seq");
           r.set_return_type(org.Biocorba.Seqcore.SeqHelper.type());
           org.omg.CORBA.Any _primary_id = r.add_in_arg();
           _primary_id.insert_string(primary_id);
           r.exceptions().add(org.Biocorba.Seqcore.UnableToProcessHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(org.Biocorba.Seqcore.UnableToProcessHelper.type())) {
                   throw org.Biocorba.Seqcore.UnableToProcessHelper.extract(__userEx.except);
               }
           }
           org.Biocorba.Seqcore.Seq __result;
           __result = org.Biocorba.Seqcore.SeqHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqDB::get_primaryidList
    public String[] get_primaryidList()
 {
           org.omg.CORBA.Request r = _request("get_primaryidList");
           r.set_return_type(org.Biocorba.Seqcore.primaryidListHelper.type());
           r.invoke();
           String[] __result;
           __result = org.Biocorba.Seqcore.primaryidListHelper.extract(r.return_value());
           return __result;
   }

};
