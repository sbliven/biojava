/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/_PRIMARYSEQSTUB.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public class _PrimarySeqStub
	extends org.omg.CORBA.portable.ObjectImpl
    	implements org.Biocorba.Seqcore.PrimarySeq {

    public _PrimarySeqStub(org.omg.CORBA.portable.Delegate d) {
          super();
          _set_delegate(d);
    }

    private static final String _type_ids[] = {
        "IDL:org/Biocorba/Seqcore/PrimarySeq:1.0",
        "IDL:org/Biocorba/Seqcore/AnonymousSeq:1.0",
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
    //	    Implementation of ::org::Biocorba::Seqcore::AnonymousSeq::type
    public org.Biocorba.Seqcore.SeqType type()
 {
           org.omg.CORBA.Request r = _request("type");
           r.set_return_type(org.Biocorba.Seqcore.SeqTypeHelper.type());
           r.invoke();
           org.Biocorba.Seqcore.SeqType __result;
           __result = org.Biocorba.Seqcore.SeqTypeHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::AnonymousSeq::length
    public int length()
 {
           org.omg.CORBA.Request r = _request("length");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::AnonymousSeq::get_seq
    public String get_seq()
        throws org.Biocorba.Seqcore.RequestTooLarge {
           org.omg.CORBA.Request r = _request("get_seq");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.exceptions().add(org.Biocorba.Seqcore.RequestTooLargeHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(org.Biocorba.Seqcore.RequestTooLargeHelper.type())) {
                   throw org.Biocorba.Seqcore.RequestTooLargeHelper.extract(__userEx.except);
               }
           }
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::AnonymousSeq::get_subseq
    public String get_subseq(int start, int end)
        throws org.Biocorba.Seqcore.OutOfRange, org.Biocorba.Seqcore.RequestTooLarge {
           org.omg.CORBA.Request r = _request("get_subseq");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           org.omg.CORBA.Any _start = r.add_in_arg();
           _start.insert_long(start);
           org.omg.CORBA.Any _end = r.add_in_arg();
           _end.insert_long(end);
           r.exceptions().add(org.Biocorba.Seqcore.OutOfRangeHelper.type());
           r.exceptions().add(org.Biocorba.Seqcore.RequestTooLargeHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(org.Biocorba.Seqcore.OutOfRangeHelper.type())) {
                   throw org.Biocorba.Seqcore.OutOfRangeHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(org.Biocorba.Seqcore.RequestTooLargeHelper.type())) {
                   throw org.Biocorba.Seqcore.RequestTooLargeHelper.extract(__userEx.except);
               }
           }
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::AnonymousSeq::max_request_length
    public int max_request_length()
 {
           org.omg.CORBA.Request r = _request("max_request_length");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::PrimarySeq::display_id
    public String display_id()
 {
           org.omg.CORBA.Request r = _request("display_id");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::PrimarySeq::primary_id
    public String primary_id()
 {
           org.omg.CORBA.Request r = _request("primary_id");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::PrimarySeq::accession_number
    public String accession_number()
 {
           org.omg.CORBA.Request r = _request("accession_number");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::PrimarySeq::version
    public int version()
 {
           org.omg.CORBA.Request r = _request("version");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }

};
