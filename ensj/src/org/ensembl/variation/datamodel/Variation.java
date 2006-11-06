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
 * A nucleotide variation such as a SNP.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface Variation extends Persistent {
 
  /**
   *@return synonyms which is a list of zero or more Strings.
   */
  List getSynonyms();
  
  /**
   *@return synonym sources which is a list of zero or more Strings.
   */
  List getSynonymSources();
  
  void addSynonym(String synonym);
    
  List getValidationStates();

  void addValidationState(ValidationState state);

  List getAlleles();

  void addAllele(Allele allele);

  String getFivePrimeFlankingSeq();
  void setFivePrimeFlankingSeq(String seq);

  String getThreePrimeFlankingSeq();
  void setThreePrimeFlankingSeq(String seq);

  String getName();
  void setName(String name);
  
}
