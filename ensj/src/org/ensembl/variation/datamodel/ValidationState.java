/*
    Copyright (C) 2001 EBI, GRL

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
package org.ensembl.variation.datamodel;

import java.util.HashMap;
import java.util.Map;

/**
 * Possible Validation states of a Variation.
 * 
 * Use static instances.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class ValidationState {

  private final static Map code2ValidationState = new HashMap();

  public final static ValidationState CLUSTER = new ValidationState("cluster",(byte)1);
  public final static ValidationState FREQ = new ValidationState("freq",(byte)2);
  public final static ValidationState SUBMITTER = new ValidationState("submitter",(byte)4);
  public final static ValidationState DOUBLEHIT = new ValidationState("doublehit",(byte)8);
  public final static ValidationState HAPMAP = new ValidationState("hapmap",(byte)16);

  private final byte bitFlag;

  private final String code;

  private ValidationState(String code, byte bitFlag){
    this.code = code;
    this.bitFlag = bitFlag;
    code2ValidationState.put(code,this);
  }
  
  
  /**
   * Factory method for getting instances of this class.
   * @param code Validate state code.
   * @return validation state instance.
   * @throws IllegalArgumentException if no validation state exists
   * with the specified code.
   */
  public static ValidationState createValidationState(String code) {
    ValidationState vs = (ValidationState)code2ValidationState.get(code);
    if ( vs==null) throw new IllegalArgumentException("No ValidationState exists for code: " + code);
    return vs;
  }
  
  public String toString() {
    return "[code="+code+", bitFlag="+bitFlag+"]";
  }

  /**
   * States bit flag.
   * @return bit flag of this state.
   */
  public byte getBitFlag() {
    return bitFlag;
  }

  /**
   * States code.
   * @return code for this state.
   */
  public String getCode() {
    return code;
  }

}
