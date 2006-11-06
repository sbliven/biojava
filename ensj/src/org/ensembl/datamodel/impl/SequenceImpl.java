/*
  Copyright (C) 2003 EBI, GRL

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.ensembl.datamodel.impl;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.ensembl.datamodel.Sequence;
import org.ensembl.driver.CoreDriver;


public class SequenceImpl extends LocatableImpl implements Sequence {

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;



  private static final Logger logger = Logger.getLogger(Sequence.class.getName());

  public SequenceImpl() {
  }

  public SequenceImpl(String sequence) {
    setString( sequence );
  }

  public String getString() {
  	try {
  	  return new String( byteSequence, "ASCII" );
  	} catch( UnsupportedEncodingException e ) {
  		// cannot happen
  		return null;
  	}
  }


  public void setString(String sequence) {
  	try {
      this.byteSequence = sequence.getBytes( "ASCII" );
  	} catch( UnsupportedEncodingException e ) {
  		// cannot happen
  	}
  }

  public void setSequence(Sequence sequence) {
    this.location = sequence.getLocation();
    this.setString( sequence.getString());
  }
  
  /**
   * The offsets are relative to the first base in the sequence which is
   * 1. The positions are inclusive; paramaters subSequence(1,1) returns the
   * first base and subSequence(3,19) returns 17 bases starting at the third.
   * @param relativeStart first base to be included in returned
   * sequence.
   * @param relativeEnd last base to included in returned sequence. This
   * number is relative to the first base.
   * @return sub sequence derived from this sequence this will include a location if
   * location is set in the current Sequence.
   */
  public Sequence subSequence(int relativeStart, int relativeEnd) {
  	Sequence seq = new SequenceImpl( this.getString().substring( relativeStart-1, relativeEnd));
  	final int start = relativeStart-1;
  	final int len = relativeEnd-start;
    seq.setLocation(location.relative(start, len));
  	return seq;
  }


	/**
	 * @return this.
	 */
	public Sequence getSequence() {
		return this;
	}


	public CoreDriver getDriver() {
	return driver;
	}

	public void setDriver(CoreDriver driver) {
		this.driver = driver;
	}

  public String toString() {
        StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append("{").append(super.toString()).append("}, ");
    buf.append("sequence=").append(getString()).append(", ");
    buf.append("]");

    return buf.toString();
  }

  private byte[] byteSequence = null;
  private transient CoreDriver driver;
}
