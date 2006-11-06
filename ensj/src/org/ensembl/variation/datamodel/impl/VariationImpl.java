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
package org.ensembl.variation.datamodel.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.RuntimeAdaptorException;
import org.ensembl.util.StringUtil;
import org.ensembl.variation.datamodel.Allele;
import org.ensembl.variation.datamodel.ValidationState;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.driver.VariationDriver;

/**
 * A sequence variation.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class VariationImpl extends PersistentImpl implements Variation {

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



  private List validationStates = new ArrayList();


  private List synonyms = new ArrayList();

  private List synonymSources = null;

  private List alleles = new ArrayList();

  private VariationDriver vdriver;

  private String name;

  private String threePrimeFlankingSequence;
  
  private String fivePrimeFlankingSequence;

  /**
   * This empty constructor is designed to be used by 
   * VariationFeatureImpl only.
   */
  VariationImpl() {
    
  }
  
  
  public VariationImpl(VariationDriver vdriver) {
    this.vdriver = vdriver;
  }



  /**
   * @see org.ensembl.variation.datamodel.Variation#getSynonyms()
   */
  public List getSynonyms() {
    return synonyms;
  }

  /* (non-Javadoc)
   * @see org.ensembl.variation.datamodel.Variation#getSynonymSources()
   */
  public List getSynonymSources() {
    if (synonymSources==null) {
      Set buf = new HashSet();
      for (int i = 0; i < synonyms.size(); i++) {
        String s = (String) synonyms.get(i);
        buf.add(s.split(":")[1]); // format is synonym:source
      }
      synonymSources = new ArrayList(buf);
    }
    return synonymSources;
  }

  /**
   * @see org.ensembl.variation.datamodel.Variation#addSynonym(java.lang.String)
   */
  public void addSynonym(String synonym) {
    synonyms.add(synonym);
    
  }

  /* (non-Javadoc)
   * @see org.ensembl.variation.datamodel.Variation#getValidationStates()
   */
  public List getValidationStates() {
    return validationStates;
  }

  /* (non-Javadoc)
   * @see org.ensembl.variation.datamodel.Variation#addValidationState(org.ensembl.variation.datamodel.ValidationState)
   */
  public void addValidationState(ValidationState state) {
    validationStates.add(state);
    
  }


  /* (non-Javadoc)
   * @see org.ensembl.variation.datamodel.Variation#getAlleles()
   */
  public List getAlleles() {
    return alleles;
  }

  /* (non-Javadoc)
   * @see org.ensembl.variation.datamodel.Variation#addAllele(org.ensembl.variation.datamodel.Allele)
   */
  public void addAllele(Allele allele) {
    alleles.add(allele);
  }

  /* (non-Javadoc)
   * @see org.ensembl.variation.datamodel.Variation#getFivePrimeFlankingSeq()
   */
  public String getFivePrimeFlankingSeq() {
    if (fivePrimeFlankingSequence==null && vdriver!=null)
      lazyLoadFlankingSequence();
    return fivePrimeFlankingSequence;
  }

  public void setFivePrimeFlankingSeq(String seq) {
    fivePrimeFlankingSequence = seq;
  }
  
  /* (non-Javadoc)
   * @see org.ensembl.variation.datamodel.Variation#getThreePrimeFlankingSeq()
   */
  public String getThreePrimeFlankingSeq() {
    if (threePrimeFlankingSequence==null && vdriver!=null)
      lazyLoadFlankingSequence();
    return threePrimeFlankingSequence;
  }

  public void setThreePrimeFlankingSeq(String seq) {
    threePrimeFlankingSequence = seq;
  }
  
  private void lazyLoadFlankingSequence() {
    
    try {
      vdriver.getVariationAdaptor().fetchFlankingSequence(this);
    } catch(AdaptorException e) {
      throw new RuntimeAdaptorException(e);
    }
    
  }


  /**
   * @see org.ensembl.variation.datamodel.Variation#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * @see org.ensembl.variation.datamodel.Variation#setName(String)
   */
  public void setName(String name) {
    this.name=name;    
  }

  
  /**
   * @see org.ensembl.datamodel.impl.PersistentImpl#toString()
   */
  public String toString() {
    StringBuffer buf = new StringBuffer("[");
    buf.append(super.toString());
    buf.append("vdiver=").append(StringUtil.setOrUnset(vdriver));
    buf.append(", name=").append(name);
    buf.append(", synonyms=").append(StringUtil.sizeOrUnset(synonyms));
    buf.append(", validationStates=").append(StringUtil.sizeOrUnset(validationStates));
    return buf.append("]").toString();
  }
}
