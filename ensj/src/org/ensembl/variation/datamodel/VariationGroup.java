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

import java.util.List;

import org.ensembl.datamodel.Persistent;

/**
 * Grouping of variations (aka haplotype set).
 * 
 * A grouping of variations that have tight linkage.  This is commonly known
 * as a Haplotype Set.  It can be viewed as a vertical grouping of
 * AlleleGroups.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface VariationGroup extends Persistent {

  /**
   * name of this variation group
   * @return name of this variation group
   */
  String getName();

  /**
   * Set the name of this variation group.
   * @param name name of this variation group
   * @see #getName()
   */
  void setName(String name);
  
  /**
   * The name of the database this variation group is from
   * @return database name this group come from.
   */
  String getSource();

  /**
   * @param source name of source database this group come from.
   * @see #getSource()
   */
  void setSource(String source);
  
  /**
   * Type of this group.  Must be either 'haplotype' or 'tag'.
   * @return Type of this group, either 'haplotype' or 'tag'.
   */
  String getType();

  /**
   * Set the type of this group.  Must be either 'haplotype' or 'tag'.
   * @param type either 'haplotype' or 'tag'.
   */
  void setType(String type);
  
  /**
   * Variations which make up this group.
   * @return zero or more variations.
   */
  List getVariations();

  /**
   * Add variation to group.
   * @param variation variation to add to group.
   */
  void addVariation(Variation variation);
}
