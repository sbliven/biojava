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

import org.ensembl.compara.datamodel.ComparaDataFactory;
import org.ensembl.compara.datamodel.DnaFragment;
import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.compara.datamodel.GenomicAlign;
import org.ensembl.compara.datamodel.GenomicAlignBlock;
import org.ensembl.compara.datamodel.Homology;
import org.ensembl.compara.datamodel.Member;
import org.ensembl.compara.datamodel.MethodLink;
import org.ensembl.compara.datamodel.MethodLinkSpeciesSet;
import org.ensembl.compara.driver.impl.ComparaDriverImpl;
import org.ensembl.datamodel.DnaDnaAlignFeature;
import org.ensembl.datamodel.FeaturePair;
import org.ensembl.datamodel.impl.DnaDnaAlignFeatureImpl;
import org.ensembl.datamodel.impl.FeaturePairImpl;

public class ComparaDataFactoryImpl implements ComparaDataFactory {
  private final ComparaDriverImpl driver;

  public ComparaDataFactoryImpl(){
    driver = null;
  }

  public ComparaDataFactoryImpl(ComparaDriverImpl driver) {
    this.driver = driver;
  }

  public GenomicAlign createGenomicAlign() {
    return new GenomicAlignImpl();
  }

  public GenomicAlignBlock createGenomicAlignBlock() {
    return new GenomicAlignBlockImpl();
  }
  
  public GenomeDB createGenomeDB() {
    return new GenomeDBImpl();
  }
  
  public DnaFragment createDnaFragment() {
    return new DnaFragmentImpl();
  }
  
  public FeaturePair createFeaturePair() {
    return new FeaturePairImpl();
  }
  
  public DnaDnaAlignFeature createDnaDnaAlignFeature() {
    return new DnaDnaAlignFeatureImpl();
  }//end createDnaDnaAlignFeature
  
  public MethodLink createMethodLink() {
    return new MethodLinkImpl();
  }//end createMethodLink
  
  public MethodLinkSpeciesSet createMethodLinkSpeciesSet(){
    return new MethodLinkSpeciesSetImpl();
  }
  
  public Homology createHomology(){
    return new HomologyImpl();
  }

  public Member createMember(){
    return new MemberImpl();
  }
} // EnsemblDataFactoryImpl 