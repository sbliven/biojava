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

package org.ensembl.datamodel;


/**
 * A QtlFeature represent a region where the Qtl appears in the Ensembl
 * database.
 * @see org.ensembl.datamodel.Qtl
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface QtlFeature extends Feature {

  /**
   * Qtl this feature is associated with.
   * @return Qtl this feature is associated with.
   */
  Qtl getQtl();
  
  /**
   * Qtl this feature is associated with.
   * @param qtl Qtl this feature is associated with.
   * 
   */
  void setQtl(Qtl qtl);

  /**
   * Set the internal ID of the QTL this feature represents. 
   * @param qtlID internal ID of the QTL this feature represents.
   */
  void setQtlID(long qtlID);

  /**
   * Get the internal ID of the QTL this feature represents. 
   * @return internal ID of the QTL this feature represents.
   */
  long getQtlID();
}
