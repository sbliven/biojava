package org.ensembl.compara.driver.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.ensembl.compara.datamodel.DnaFragment;
import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.compara.datamodel.GenomicAlign;
import org.ensembl.compara.datamodel.GenomicAlignBlock;
import org.ensembl.compara.datamodel.MethodLink;
import org.ensembl.compara.datamodel.MethodLinkSpeciesSet;
import org.ensembl.compara.driver.DnaDnaAlignFeatureAdaptor;
import org.ensembl.compara.driver.DnaFragmentAdaptor;
import org.ensembl.compara.driver.FatalException;
import org.ensembl.compara.driver.GenomeDBAdaptor;
import org.ensembl.compara.driver.GenomicAlignAdaptor;
import org.ensembl.compara.driver.GenomicAlignBlockAdaptor;
import org.ensembl.compara.driver.MethodLinkAdaptor;
import org.ensembl.compara.driver.MethodLinkSpeciesSetAdaptor;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.DnaDnaAlignFeature;
import org.ensembl.datamodel.Feature;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.util.NotImplementedYetException;


/**
 * Fetches DnaDnaAlignFeature objects.
 * @author <a href="maito:vvi@sanger.ac.uk">Vivek Iyer</a>
 * Need fetch(Location), fetch(internalId), and some mess involving analysis_id_conditions (!!)
**/
public class 
  DnaDnaAlignFeatureAdaptorImpl 
extends 
  ComparaBaseAdaptor
implements 
  DnaDnaAlignFeatureAdaptor
{
  private HashMap dnaFragmentHash = new HashMap();

  public DnaDnaAlignFeatureAdaptorImpl(ComparaDriverImpl driver) {
    super(driver);
  }//end DnaDnaAlignFeatureAdaptorImpl

  public String getType(){
    return TYPE;
  }
    
  /**
   * Not implemented yet!
  **/
  public void store( Feature feature ) throws  AdaptorException {
    throw new NotImplementedYetException("Store not implemented on DnaDnaAlignFeatureAdaptor");
  }//end store

  public List fetch(
      String consensusSpecies, 
      Location consensusLocation, 
      String querySpecies
  ) throws AdaptorException {
    
    return fetch(
      consensusSpecies, 
      consensusLocation, 
      querySpecies,
      null
    );
    
  }
  
  /**
   * <p>Returns a list of Dna-Dna alignment features for the input
   * query species, chromosome location and hit species. Namely, </p>
   * <ol>
   * <li> Fetches all DnaFragments for the input location and species </li>
   * <li> For each DnaFragment - retrieve the genomic aligns associated to that fragment
   * and consensus/query species</li>
   * <li> For each retrieved genomic align, convert to a dnadnaalignfeature, and pass out.</li>
   * </ol>
  **/
  public List fetch(String consensusSpecies, Location consensusLocation, String querySpecies, String methodLinkType) throws AdaptorException {
    
    List returnList = new ArrayList();
    List dnaFragments;
    DnaFragment dnaFragment;
    MethodLinkSpeciesSet methodLinkSpeciesSet;
    Iterator methodLinkSpeciesSetIter;
    List dnaDnaAlignFeaturesForAlignBlockList;
    int start = consensusLocation.getStart();
    int end = consensusLocation.getEnd();
    List genomicAlignsInBlock;
    List genomicAlignBlocks;
    Iterator genomicAlignBlocksForDnaFragment;
    GenomicAlignBlock genomicAlignBlock;
    GenomicAlign consensusGenomicAlign;
    GenomicAlign queryGenomicAlign;
    DnaDnaAlignFeature dnaDnaAlignFeature;
    
    GenomeDBAdaptor genomeDBAdaptor = 
      (GenomeDBAdaptor)getDriver().getAdaptor(GenomeDBAdaptorImpl.TYPE);
    GenomicAlignBlockAdaptor genomicAlignBlockAdaptor =
      (GenomicAlignBlockAdaptor)getDriver().getAdaptor(GenomicAlignBlockAdaptorImpl.TYPE);
    GenomicAlignAdaptor genomicAlignAdaptor =
      (GenomicAlignAdaptor)getDriver().getAdaptor(GenomicAlignAdaptorImpl.TYPE);
      
    GenomeDB consensusGenome = genomeDBAdaptor.fetch(consensusSpecies);
    
    if(consensusGenome == null){
      throw new AdaptorException("Can't find genome for species name: "+consensusSpecies);
    }

    GenomeDB targetGenome = null;
    GenomeDB[] genomes = null;
    if(querySpecies != null){
      targetGenome = genomeDBAdaptor.fetch(querySpecies);
      if(targetGenome == null){
        throw new AdaptorException("Can't find genome for species name: "+querySpecies);
      }
    }
    
    genomes = getGenomes(consensusGenome, targetGenome);
            
    getLogger().fine("Fetching dna frags:");

    dnaFragment = getDnaFrag(consensusGenome, consensusLocation);
    
    getLogger().fine("Fetching methodLinkSpeciesSet:");

//    if(methodLinkType == null){
//      throw new AdaptorException("Must pass in a methodLinktype !");
//    }
      
    methodLinkSpeciesSetIter = getMethodLinkSpeciesSets(methodLinkType, genomes).iterator();
    
    getLogger().fine("fetching genomic aligns for dna frag");
    
    if(start < 1){
      start = 1;
    }

    if(dnaFragment.getLength() < end){
      end = dnaFragment.getLength();
    }

    while (methodLinkSpeciesSetIter.hasNext()) {
      methodLinkSpeciesSet = (MethodLinkSpeciesSet)methodLinkSpeciesSetIter.next();

      System.out.println("Fetching for method link species set = " + methodLinkSpeciesSet);
      genomicAlignBlocks = genomicAlignBlockAdaptor.fetch(
          methodLinkSpeciesSet,
          dnaFragment,
          start,
          end
        );
  
      genomicAlignBlocksForDnaFragment = genomicAlignBlocks.iterator();
  
      while(genomicAlignBlocksForDnaFragment.hasNext()){
        genomicAlignBlock = (GenomicAlignBlock)genomicAlignBlocksForDnaFragment.next();
        genomicAlignsInBlock = genomicAlignBlock.getAllGenomicAligns();
        if(genomicAlignsInBlock.size() != 2){
          throw new AdaptorException("Must have two genomic aligns in block "+genomicAlignBlock.getInternalID()+" for DnaDnaAlignFeature fetch");
        }
        if(((GenomicAlign)genomicAlignsInBlock.get(0)).getDnaFragmentId() == dnaFragment.getInternalID()){
          consensusGenomicAlign = (GenomicAlign)genomicAlignsInBlock.get(0);
          queryGenomicAlign = (GenomicAlign)genomicAlignsInBlock.get(1);
        }else if(((GenomicAlign)genomicAlignsInBlock.get(1)).getDnaFragmentId() == dnaFragment.getInternalID()){
          consensusGenomicAlign = (GenomicAlign)genomicAlignsInBlock.get(1);
          queryGenomicAlign = (GenomicAlign)genomicAlignsInBlock.get(0);
        }else{
          throw new FatalException(
              "Recovered a genomic align block: "+genomicAlignBlock.getInternalID()+" having genomic aligns containing dna frag: "+
              dnaFragment.getInternalID()+" in neither position 0 or 1"
          );
        }
        
        dnaDnaAlignFeature = 
          createDnaDnaAlignFeature(
            consensusSpecies,
            querySpecies,
            dnaFragment,
            genomicAlignBlock,
            consensusGenomicAlign,
            queryGenomicAlign,
            methodLinkSpeciesSet.getMethodLink().getType()
          );
        
        returnList.add(dnaDnaAlignFeature);
      }
    }
    
    return returnList;
  }//end fetch
  

  private GenomeDB[] getGenomes(GenomeDB consensusGenome, GenomeDB targetGenome) throws AdaptorException {
    GenomeDB[] genomes = null;
    if(targetGenome != null){
      genomes = new GenomeDB[2];
      genomes[0] = consensusGenome;
      genomes[1] = targetGenome;
    }else{
      genomes = new GenomeDB[1];
      genomes[0] = consensusGenome;
    }
    
    return genomes;
  }
  
  private DnaFragment getDnaFrag(GenomeDB consensusGenome, Location consensusLocation) throws AdaptorException {

    DnaFragmentAdaptor dnaFragmentAdaptor = 
      (DnaFragmentAdaptor)getDriver().getAdaptor(DnaFragmentAdaptorImpl.TYPE);
    
    
    List dnaFragments =
      dnaFragmentAdaptor.fetch(
        consensusGenome, 
        null,//don't care which coord system the dnafrag corresponds to 
        consensusLocation.getSeqRegionName()
      );
    
    getLogger().fine("Finished fetching dna frags:"+dnaFragments.size());
    
    if(dnaFragments.size()>1){
      throw new AdaptorException(
        "More than one dna fragment for fragment name: "+consensusLocation.getSeqRegionName()+
        " - this isn't supposed to happen!"
      );
    }
    
    if(dnaFragments.size()<=0){
      throw new AdaptorException(
        "NO dna fragment for fragment name: "+consensusLocation.getSeqRegionName()+
        " - this isn't supposed to happen!"
      );
    }
    
    return (DnaFragment)dnaFragments.get(0);
    
  }
  
  private List getMethodLinkSpeciesSets(String methodLinkType, GenomeDB[] genomes) throws AdaptorException{
    MethodLink methodLink = null;
    List methodLinkSpeciesSets;
    
    MethodLinkAdaptor methodLinkAdaptor = 
      (MethodLinkAdaptor)getDriver().getAdaptor(MethodLinkAdaptor.TYPE);
    
    MethodLinkSpeciesSetAdaptor methodLinkSpeciesSetAdaptor = 
      (MethodLinkSpeciesSetAdaptor)getDriver().getAdaptor(MethodLinkSpeciesSetAdaptor.TYPE);
    
    if(methodLinkType != null){
      methodLink = methodLinkAdaptor.fetch(methodLinkType);
      if(methodLink == null){
        throw new AdaptorException("Can't find method link with type: "+methodLinkType);
      }
    }
    
    methodLinkSpeciesSets = methodLinkSpeciesSetAdaptor.fetch(methodLink, genomes);

    if(methodLinkSpeciesSets.size() <= 0){
      throw new AdaptorException("Found no method link species sets for input genomes and alignment type");
    }
    
    //if(methodLinkSpeciesSets.size() > 1){
    //  new Throwable().printStackTrace();
    //  throw new AdaptorException("Found more than one method link species sets for input genomes and alignment type");
    //}
    
    //return (MethodLinkSpeciesSet) methodLinkSpeciesSets.get(0);
    return methodLinkSpeciesSets;
  }
  
  private DnaDnaAlignFeature createDnaDnaAlignFeature(
    String consensusSpecies,
    String querySpecies,
    DnaFragment frag,
    GenomicAlignBlock block,
    GenomicAlign consensusAlign,
    GenomicAlign queryAlign,
    String methodLinkType
  ) throws AdaptorException {
    DnaDnaAlignFeature dnaDnaAlign = getFactory().createDnaDnaAlignFeature();
    CoordinateSystem consensusCoordSystem = 
      new CoordinateSystem("Fake coord system to populate consensus locations produced by compara");
    CoordinateSystem hitCoordSystem = 
      new CoordinateSystem("Fake coord system to populate hit locations produced by compara");
    dnaDnaAlign.setInternalID(consensusAlign.getInternalID());
    dnaDnaAlign.setDisplayName(queryAlign.getDnaFragment().getName());
    dnaDnaAlign.setLocation(new Location(consensusCoordSystem, consensusAlign.getDnaFragment().getName(), consensusAlign.getStart(), consensusAlign.getEnd(), 1));
    dnaDnaAlign.setSpecies(consensusSpecies);
    if(querySpecies == null){
      dnaDnaAlign.setHitSpecies(queryAlign.getDnaFragment().getGenomeDB().getName());
    }else{
      dnaDnaAlign.setHitSpecies(querySpecies);
    }
    dnaDnaAlign.setScore(block.getScore());
    dnaDnaAlign.setPercentageIdentity(block.getPercentageID());    
    dnaDnaAlign.setHitDisplayName(consensusAlign.getDnaFragment().getName());
    dnaDnaAlign.setHitLocation(new Location(hitCoordSystem, queryAlign.getDnaFragment().getName(), queryAlign.getStart(), queryAlign.getEnd(), 1));

    dnaDnaAlign.setMethodLinkType(methodLinkType);
    dnaDnaAlign.setDriver(getDriver());
    dnaDnaAlign.setCigarString(consensusAlign.getCigarString());
    return dnaDnaAlign;
  }
  
  /**
   * A hash to cache DnaFragments based on internal id. As a stateful property,
   * this is harmless - very unlikely that there's a problem in it getting stored across
   * successive invocations.
  **/
  private HashMap getDnaFragmentHash(){
    return  dnaFragmentHash;
  }//end getDnaFragmentHash

  protected void configure(){
    //do nothing
  }
  
  protected  String getTableName(){
    if(true){
      throw new IllegalStateException("This adaptor should not be writing this table");
    }
    return null;
  }
  
  protected HashMap mapObjectToColumns(Persistent object){
    if(true){
      throw new IllegalStateException("This adaptor should not be writing this table");
    }
    return null;
  }
  
  protected void mapColumnsToObject(HashMap columns, Persistent object){
    if(true){
      throw new IllegalStateException("This adaptor should not be writing this table");
    }
  }
  
  protected PersistentImpl createNewObject(){
    if(true){
      throw new IllegalStateException("This adaptor should not be writing this table");
    }
    return null;
  }//end createNewObject
  
  protected void validate(Persistent object) throws AdaptorException{
    if(true){
      throw new IllegalStateException("This adaptor should not be writing this table");
    }
  }
  
  public HashMap getLogicalKeyPairs(Persistent genomeDB) throws AdaptorException{
    if(true){
      throw new IllegalStateException("This adaptor should not be writing this table");
    }
    return null;
  }
}//end DnaDnaAlignFeatureAdaptorImpl 
