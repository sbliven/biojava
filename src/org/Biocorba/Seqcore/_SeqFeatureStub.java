/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/_SEQFEATURESTUB.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public class _SeqFeatureStub
	extends org.omg.CORBA.portable.ObjectImpl
    	implements org.Biocorba.Seqcore.SeqFeature {

    public _SeqFeatureStub(org.omg.CORBA.portable.Delegate d) {
          super();
          _set_delegate(d);
    }

    private static final String _type_ids[] = {
        "IDL:org/Biocorba/Seqcore/SeqFeature:1.0",
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
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeature::type
    public String type()
 {
           org.omg.CORBA.Request r = _request("type");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeature::source
    public String source()
 {
           org.omg.CORBA.Request r = _request("source");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeature::seq_primary_id
    public String seq_primary_id()
 {
           org.omg.CORBA.Request r = _request("seq_primary_id");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeature::start
    public int start()
 {
           org.omg.CORBA.Request r = _request("start");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeature::end
    public int end()
 {
           org.omg.CORBA.Request r = _request("end");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeature::strand
    public short strand()
 {
           org.omg.CORBA.Request r = _request("strand");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
           r.invoke();
           short __result;
           __result = r.return_value().extract_short();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeature::qualifiers
    public org.Biocorba.Seqcore.NameValueSet[] qualifiers()
 {
           org.omg.CORBA.Request r = _request("qualifiers");
           r.set_return_type(org.Biocorba.Seqcore.NameValueSetListHelper.type());
           r.invoke();
           org.Biocorba.Seqcore.NameValueSet[] __result;
           __result = org.Biocorba.Seqcore.NameValueSetListHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeature::PrimarySeq_is_available
    public boolean PrimarySeq_is_available()
 {
           org.omg.CORBA.Request r = _request("PrimarySeq_is_available");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           r.invoke();
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeature::get_PrimarySeq
    public org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq()
        throws org.Biocorba.Seqcore.UnableToProcess {
           org.omg.CORBA.Request r = _request("get_PrimarySeq");
           r.set_return_type(org.Biocorba.Seqcore.PrimarySeqHelper.type());
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

};
