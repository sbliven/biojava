/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQFEATURESTUB.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public class _SeqFeatureStub
	extends org.omg.CORBA.portable.ObjectImpl
    	implements org.biocorba.Bio.SeqFeature {

    public _SeqFeatureStub(org.omg.CORBA.portable.Delegate d) {
          super();
          _set_delegate(d);
    }

    private static final String _type_ids[] = {
        "IDL:Bio/SeqFeature:1.0",
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
    //	    Implementation of ::Bio::SeqFeature::type
    public String type()
 {
           org.omg.CORBA.Request r = _request("type");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::Bio::SeqFeature::source
    public String source()
 {
           org.omg.CORBA.Request r = _request("source");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::Bio::SeqFeature::seq_primary_id
    public String seq_primary_id()
 {
           org.omg.CORBA.Request r = _request("seq_primary_id");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::Bio::SeqFeature::start
    public int start()
 {
           org.omg.CORBA.Request r = _request("start");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }
    //	    Implementation of ::Bio::SeqFeature::end
    public int end()
 {
           org.omg.CORBA.Request r = _request("end");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }
    //	    Implementation of ::Bio::SeqFeature::strand
    public short strand()
 {
           org.omg.CORBA.Request r = _request("strand");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
           r.invoke();
           short __result;
           __result = r.return_value().extract_short();
           return __result;
   }
    //	    Implementation of ::Bio::SeqFeature::qualifiers
    public org.biocorba.Bio.NameValueSet[] qualifiers()
 {
           org.omg.CORBA.Request r = _request("qualifiers");
           r.set_return_type(org.biocorba.Bio.NameValueSetListHelper.type());
           r.invoke();
           org.biocorba.Bio.NameValueSet[] __result;
           __result = org.biocorba.Bio.NameValueSetListHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::Bio::SeqFeature::has_PrimarySeq
    public boolean has_PrimarySeq()
 {
           org.omg.CORBA.Request r = _request("has_PrimarySeq");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           r.invoke();
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }
    //	    Implementation of ::Bio::SeqFeature::get_PrimarySeq
    public org.biocorba.Bio.PrimarySeq get_PrimarySeq()
        throws org.biocorba.Bio.UnableToProcess {
           org.omg.CORBA.Request r = _request("get_PrimarySeq");
           r.set_return_type(org.biocorba.Bio.PrimarySeqHelper.type());
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

};
