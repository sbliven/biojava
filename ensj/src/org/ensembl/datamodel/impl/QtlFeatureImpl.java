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

import org.ensembl.datamodel.Qtl;
import org.ensembl.datamodel.QtlFeature;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.RuntimeAdaptorException;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class QtlFeatureImpl extends BaseFeatureImpl implements QtlFeature {

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



  private Qtl qtl;
  private long qtlID;
  
  public QtlFeatureImpl() {
    super();
  }

  public QtlFeatureImpl(CoreDriver driver) {
    super(driver);
  }

  public Qtl getQtl() {
    if (qtl==null && qtlID>0 && driver!=null)
      try {
        qtl = driver.getQtlAdaptor().fetch(qtlID);
      } catch (AdaptorException e) {
        throw new RuntimeAdaptorException(e);
      }
    return qtl;
  }

  public void setQtl(Qtl qtl) {
    this.qtl = qtl;
    qtlID = qtl.getInternalID();
  }

  /**
   * @see org.ensembl.datamodel.QtlFeature#setQtlID(long)
   */
  public void setQtlID(long qtlID) {
    this.qtlID = qtlID;
  }

  /**
   * @see org.ensembl.datamodel.QtlFeature#getQtlID()
   */
  public long getQtlID() {
    return qtlID;
  }

}
