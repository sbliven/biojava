/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQDBSTUB.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class _SeqDBStub
	extends org.omg.CORBA.portable.ObjectImpl
    	implements org.biocorba.Bio.SeqDB {

    public _SeqDBStub(org.omg.CORBA.portable.Delegate d) {
          super();
          _set_delegate(d);
    }

    private static final String _type_ids[] = {
        "IDL:Bio/SeqDB:1.0",
        "IDL:Bio/PrimarySeqDB:1.0",
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
    //	    Implementation of ::Bio::PrimarySeqDB::database_name
    public String database_name()
 {
           org.omg.CORBA.Request r = _request("database_name");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeqDB::database_version
    public short database_version()
 {
           org.omg.CORBA.Request r = _request("database_version");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
           r.invoke();
           short __result;
           __result = r.return_value().extract_short();
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeqDB::make_stream
    public org.biocorba.Bio.PrimarySeqStream make_stream()
 {
           org.omg.CORBA.Request r = _request("make_stream");
           r.set_return_type(org.biocorba.Bio.PrimarySeqStreamHelper.type());
           r.invoke();
           org.biocorba.Bio.PrimarySeqStream __result;
           __result = org.biocorba.Bio.PrimarySeqStreamHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeqDB::get_PrimarySeq
    public org.biocorba.Bio.PrimarySeq get_PrimarySeq(String primary_id)
        throws org.biocorba.Bio.UnableToProcess {
           org.omg.CORBA.Request r = _request("get_PrimarySeq");
           r.set_return_type(org.biocorba.Bio.PrimarySeqHelper.type());
           org.omg.CORBA.Any _primary_id = r.add_in_arg();
           _primary_id.insert_string(primary_id);
           r.exceptions().add(org.biocorba.Bio.UnableToProcessHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(org.biocorba.Bio.UnableToProcessHelper.type())) {
                   throw org.biocorba.Bio.UnableToProcessHelper.extract(__userEx.except);
               }
           }
           org.biocorba.Bio.PrimarySeq __result;
           __result = org.biocorba.Bio.PrimarySeqHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::Bio::SeqDB::get_Seq
    public org.biocorba.Bio.Seq get_Seq(String primary_id)
        throws org.biocorba.Bio.UnableToProcess {
           org.omg.CORBA.Request r = _request("get_Seq");
           r.set_return_type(org.biocorba.Bio.SeqHelper.type());
           org.omg.CORBA.Any _primary_id = r.add_in_arg();
           _primary_id.insert_string(primary_id);
           r.exceptions().add(org.biocorba.Bio.UnableToProcessHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(org.biocorba.Bio.UnableToProcessHelper.type())) {
                   throw org.biocorba.Bio.UnableToProcessHelper.extract(__userEx.except);
               }
           }
           org.biocorba.Bio.Seq __result;
           __result = org.biocorba.Bio.SeqHelper.extract(r.return_value());
           return __result;
   }

};
