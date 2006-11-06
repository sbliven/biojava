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
import org.ensembl.compara.datamodel.Member;
import org.ensembl.datamodel.impl.PersistentImpl;

/**
 * I represent a grouping of members based on some sort of sequence homology.
**/
public class MemberImpl extends PersistentImpl implements Member{
  
	private static final long serialVersionUID = 1L;
	private String _stableId;
  private int _version;
  private String _sourceName;
  private long _taxonId;
  private long _genomeDbId;
  private long _sequenceId;
  private long _geneMemberId;
  private String _description; 
  private String _chrName;
  private int _chrStart;
  private int _chrEnd;
  private int _chrStrand;

  public String getStableId() {
    return _stableId;
  }

  public void setStableId(String _stableId) {
    this._stableId = _stableId;
  }

  public int getVersion() {
    return _version;
  }

  public void setVersion(int _version) {
    this._version = _version;
  }

  public String getSourceName() {
    return _sourceName;
  }

  public void setSourceName(String _sourceName) {
    this._sourceName = _sourceName;
  }

  public long getTaxonId() {
    return _taxonId;
  }

  public void setTaxonId(long _taxonId) {
    this._taxonId = _taxonId;
  }

  public long getGenomeDbId() {
    return _genomeDbId;
  }

  public void setGenomeDbId(long _genomeDbId) {
    this._genomeDbId = _genomeDbId;
  }

  public long getSequenceId() {
    return _sequenceId;
  }

  public void setSequenceId(long _sequenceId) {
    this._sequenceId = _sequenceId;
  }

  public long getGeneMemberId() {
    return _geneMemberId;
  }

  public void setGeneMemberId(long _geneMemberId) {
    this._geneMemberId = _geneMemberId;
  }

  public String getDescription() {
    return _description;
  }

  public void setDescription(String _description) {
    this._description = _description;
  }

  public String getChrName() {
    return _chrName;
  }

  public void setChrName(String _chrName) {
    this._chrName = _chrName;
  }

  public int getChrStart() {
    return _chrStart;
  }

  public void setChrStart(int _chrStart) {
    this._chrStart = _chrStart;
  }

  public int getChrEnd() {
    return _chrEnd;
  }

  public void setChrEnd(int _chrEnd) {
    this._chrEnd = _chrEnd;
  }

  public int getChrStrand() {
    return _chrStrand;
  }

  public void setChrStrand(int _chrStrand) {
    this._chrStrand = _chrStrand;
  }
}//end GenomeDBImpl