/*
 * File: SRC/BIO/SEQTYPE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public final class SeqType implements org.omg.CORBA.portable.IDLEntity {
     public static final int _PROTEIN = 0,
	  		     _DNA = 1,
	  		     _RNA = 2;
     public static final SeqType PROTEIN = new SeqType(_PROTEIN);
     public static final SeqType DNA = new SeqType(_DNA);
     public static final SeqType RNA = new SeqType(_RNA);
     public int value() {
         return _value;
     }
     public static final SeqType from_int(int i)  throws  org.omg.CORBA.BAD_PARAM {
           switch (i) {
             case _PROTEIN:
                 return PROTEIN;
             case _DNA:
                 return DNA;
             case _RNA:
                 return RNA;
             default:
	              throw new org.omg.CORBA.BAD_PARAM();
           }
     }
     private SeqType(int _value){
         this._value = _value;
     }
     private int _value;
}
