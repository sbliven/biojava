/*
 * Created on 10-Nov-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.ensembl.datamodel;

import org.ensembl.datamodel.impl.BaseFeatureImpl;

/**
 * Defines how some semi-virtual sequence regions (PAR/Haplotypes) are
 * partially constructed from other sequence regions.
 * 
 * <ul>
 * <li> location = relevant part of the semi-virtual sequence region
 * <li> target = relevant part(s) of a component sequence region.
 * <li> type = type of the assembly exception; PAR or HAPlotype.
 * </ul>
 * 
 *  @see org.ensembl.driver.AssemblyExceptionAdaptor
 */
public class AssemblyException extends BaseFeatureImpl {

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


  private Location target;
	private String type;
	
	public AssemblyException(long internalID, Location org, Location linked, String type) {
	  this.internalID = internalID;
		this.location = org;
		this.target = linked;
		this.type = type;
	}
	
	
  /**
   * @return Returns the linkedLocation.
   */
  public Location getTarget() {
    return target;
  }
  
  /**
   * @return Returns the type.
   */
  public String getType() {
    return type;
  }
  
  
  /**
   * @return String representation of this instance. 
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append("[");
    buf.append(super.toString());
    buf.append(", target=").append(target);
    buf.append(", type").append(type);
    buf.append("]");

    return buf.toString();
  }
}
