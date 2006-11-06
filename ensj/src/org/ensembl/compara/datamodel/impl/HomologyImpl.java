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
import org.ensembl.compara.datamodel.Homology;
import org.ensembl.datamodel.impl.PersistentImpl;

/**
 * I represent a grouping of members based on some sort of sequence homology.
**/
public class HomologyImpl extends PersistentImpl implements Homology{
  
	private static final long serialVersionUID = 1L;
	private String _stable_id;
  private String _description;
  private long _methodLinkSpeciesSetId;
  private String _subtype;
  private double _dn;
  private double _ds;
  private double _n;
  private double _s;
  private double _lnl;
  private double _thresholdOnDs;
  
  public String toString(){
    return 
      (new StringBuffer())
        .append("Homology (")
        .append(getInternalID())
        .append(")[")
        .append("Stableid: ")
        .append(getStableId())
        .append(", Description: ")
        .append(getDescription())
        .append(", MLSSId: ")
        .append(getMethodLinkSpeciesSetId())
        .append(", Subtype: ")
        .append(getSubtype())
        .append(", Dn/Ds: ")
        .append(getDn())
        .append("/")
        .append(getDs())
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
    return _methodLinkSpeciesSetId;
  }

  public void setMethodLinkSpeciesSetId(long _methodLinkSpeciesSetId) {
    this._methodLinkSpeciesSetId = _methodLinkSpeciesSetId;
  }

  public String getSubtype() {
    return _subtype;
  }

  public void setSubtype(String _subtype) {
    this._subtype = _subtype;
  }

  public double getDn() {
    return _dn;
  }

  public void setDn(double _dn) {
    this._dn = _dn;
  }

  public double getDs() {
    return _ds;
  }

  public void setDs(double _ds) {
    this._ds = _ds;
  }

  public double getN() {
    return _n;
  }

  public void setN(double _n) {
    this._n = _n;
  }

  public double getS() {
    return _s;
  }

  public void setS(double _s) {
    this._s = _s;
  }

  public double getLnl() {
    return _lnl;
  }

  public void setLnl(double _lnl) {
    this._lnl = _lnl;
  }

  public double getThresholdOnDs() {
    return _thresholdOnDs;
  }

  public void setThresholdOnDs(double _thresholdOnDs) {
    this._thresholdOnDs = _thresholdOnDs;
  }

}//end GenomeDBImpl