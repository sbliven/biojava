/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/_SEQFEATUREITERATORSTUB.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public class _SeqFeatureIteratorStub
	extends org.omg.CORBA.portable.ObjectImpl
    	implements org.Biocorba.Seqcore.SeqFeatureIterator {

    public _SeqFeatureIteratorStub(org.omg.CORBA.portable.Delegate d) {
          super();
          _set_delegate(d);
    }

    private static final String _type_ids[] = {
        "IDL:org/Biocorba/Seqcore/SeqFeatureIterator:1.0",
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
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeatureIterator::next
    public org.Biocorba.Seqcore.SeqFeature next()
        throws org.Biocorba.Seqcore.EndOfStream, org.Biocorba.Seqcore.UnableToProcess {
           org.omg.CORBA.Request r = _request("next");
           r.set_return_type(org.Biocorba.Seqcore.SeqFeatureHelper.type());
           r.exceptions().add(org.Biocorba.Seqcore.EndOfStreamHelper.type());
           r.exceptions().add(org.Biocorba.Seqcore.UnableToProcessHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(org.Biocorba.Seqcore.EndOfStreamHelper.type())) {
                   throw org.Biocorba.Seqcore.EndOfStreamHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(org.Biocorba.Seqcore.UnableToProcessHelper.type())) {
                   throw org.Biocorba.Seqcore.UnableToProcessHelper.extract(__userEx.except);
               }
           }
           org.Biocorba.Seqcore.SeqFeature __result;
           __result = org.Biocorba.Seqcore.SeqFeatureHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::org::Biocorba::Seqcore::SeqFeatureIterator::has_more
    public boolean has_more()
 {
           org.omg.CORBA.Request r = _request("has_more");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           r.invoke();
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }

};
