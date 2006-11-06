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
package org.ensembl.compara.datamodel.impl;
import org.ensembl.compara.datamodel.Family;
import org.ensembl.datamodel.impl.PersistentImpl;

/**
 * I represent a grouping of members based on some sort of sequence homology.
**/
public class DomainImpl extends PersistentImpl implements Family{


	private static final long serialVersionUID = 1L;
	private String _stable_id;
  private String _description;
  private long _method_link_species_set_id;
  private double _description_score;

  public String toString(){
    return 
      (new StringBuffer())
        .append("Family (")
        .append(getInternalID())
        .append(")[")
        .append("Stableid: ")
        .append(getStableId())
        .append(", Description: ")
        .append(getDescription())
        .append(", DescriptionScore: ")
        .append(getDescriptionScore())
        .append(", MLSSId: ")
        .append(getMethodLinkSpeciesSetId())
        .append("]")
        .toString();
  }

  public String getStableId() {
    return _stable_id;
  }

  public void setStableId(String _stable_id) {
    this._stable_id = _stable_id;
  }

  public String getDescription() {
    return _description;
  }

  public void setDescription(String _description) {
    this._description = _description;
  }

  public long getMethodLinkSpeciesSetId() {
    return _method_link_species_set_id;
  }

  public void setMethodLinkSpeciesSetId(long _method_link_species_set_id) {
    this._method_link_species_set_id = _method_link_species_set_id;
  }

  public double getDescriptionScore() {
    return _description_score;
  }

  public void setDescriptionScore(double _description_score) {
    this._description_score = _description_score;
  }
}//end GenomeDBImpl