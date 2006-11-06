package org.ensembl.compara.driver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
import org.ensembl.compara.driver.DnaFragmentAdaptor;
import org.ensembl.compara.driver.GenomeDBAdaptor;
import org.ensembl.compara.driver.GenomicAlignAdaptor;
import org.ensembl.compara.driver.GenomicAlignBlockAdaptor;
import org.ensembl.compara.driver.MethodLinkAdaptor;
import org.ensembl.compara.driver.MethodLinkSpeciesSetAdaptor;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;


/**
 * Fetches DNA-DNA alignment information from the compara database.
 * @author <a href="maito:vvi@sanger.ac.uk">Vivek Iyer</a>
**/
public class 
  GenomicAlignAdaptorImpl 
extends 
  ComparaBaseAdaptor
implements 
  GenomicAlignAdaptor
{
  public static String TABLE_NAME = "genomic_align";
  public static String GENOMIC_ALIGN_ID = TABLE_NAME+"."+"genomic_align_id";
  public static String GENOMIC_ALIGN_BLOCK_ID = TABLE_NAME+"."+"genomic_align_block_id";
  public static String DNAFRAG_ID = TABLE_NAME+"."+"dnafrag_id";
  public static String DNAFRAG_START = TABLE_NAME+"."+"dnafrag_start";
  public static String DNAFRAG_END = TABLE_NAME+"."+"dnafrag_end";
  public static String DNAFRAG_STRAND = TABLE_NAME+"."+"dnafrag_strand";
  public static String CIGAR_LINE = TABLE_NAME+"."+"cigar_line";
  public static String LEVEL_ID = TABLE_NAME+"."+"level_id";
  public static String METHOD_LINK_SPECIES_SET_ID = TABLE_NAME+"."+"method_link_species_set_id";
  
  public static int DEFAULT_MAX_ALIGNMENT = 20000;

  public GenomicAlignAdaptorImpl(ComparaDriverImpl driver) {
    super(driver);
  }//end GenomicAlignAdaptorImpl

  public String getType(){
    return TYPE;
  }//end getType
    
  public int store(GenomicAlign genomicAlign) throws  AdaptorException {
    super.store(genomicAlign);
    return 0;
  }//end store

  public GenomicAlign fetch(long internalID ) throws AdaptorException {
    return (GenomicAlign) super.fetch(new Long(internalID));
  }//end fetch

    
  public List fetch(GenomicAlignBlock block) throws AdaptorException{
    StringBuffer select = new StringBuffer();
    StringBuffer whereClause = null;
    List arguments = new ArrayList();
    PreparedStatement statement;
    List returnList = null;
    Connection connection = getConnection();
    StringBuffer sql;

    whereClause = addEqualsClause(GENOMIC_ALIGN_BLOCK_ID, new StringBuffer());
    arguments.add(new Long(block.getInternalID()));
    sql = createSelectUpToWhere().append(whereClause.toString());

    try{
      statement = prepareStatement(connection, sql.toString());
      returnList = executeStatementAndConvertResultToPersistent(statement, arguments);
    }finally{
      close(connection);
    }
    
    inflateGenomicAligns(returnList);

    return returnList;
  }
  
  
  /**
   * Fetches all GenomicAligns corresponding to the GenomicAlignBlocks.
   * 
   * @param blocks GenomicAlignBlocks filter.
   * @return
   * @throws AdaptorException
   */
  public List fetchByBlocks(List blocks) throws AdaptorException {
    StringBuffer select = new StringBuffer();
    StringBuffer whereClause = null;
    List arguments = new ArrayList();
    PreparedStatement statement;
    List returnList = new ArrayList();
    Connection connection = getConnection();
    StringBuffer sql;
    Iterator blockList = blocks.iterator();
    HashMap blockHash = new HashMap();

    if (blocks.size() == 0) {
      return returnList;
    }



    whereClause = new StringBuffer(" where " + GENOMIC_ALIGN_BLOCK_ID + " in (");
    while (blockList.hasNext()) {
      GenomicAlignBlock block = (GenomicAlignBlock)blockList.next();
      blockHash.put(new Long(block.getInternalID()), block);
      whereClause.append("" + block.getInternalID());
      if (blockList.hasNext()) whereClause.append(",");
    }
    whereClause.append(")");

    sql = createSelectUpToWhere().append(whereClause.toString());

    try{
      statement = prepareStatement(connection, sql.toString());
      returnList = executeStatementAndConvertResultToPersistent(statement, arguments);
    }finally{
      close(connection);
    }
    
    inflateGenomicAlignsWithBlockCache(returnList,blockHash);

    return returnList;
  }

  private void inflateGenomicAlignsWithBlockCache(List aligns,HashMap blockHash) throws AdaptorException{
    MethodLinkSpeciesSetAdaptor methodLinkSpeciesSetAdaptor = 
        (MethodLinkSpeciesSetAdaptor)getDriver().getAdaptor(MethodLinkSpeciesSetAdaptor.TYPE);
    
    GenomicAlignBlockAdaptor genomicAlignBlockAdaptor = 
        (GenomicAlignBlockAdaptor)getDriver().getAdaptor(GenomicAlignBlockAdaptor.TYPE);
    
    DnaFragmentAdaptor fragAdaptor = 
        (DnaFragmentAdaptor)getDriver().getAdaptor(DnaFragmentAdaptor.TYPE);
    
    HashMap mlSSHash = new HashMap();
    HashMap fragHash = new HashMap();
    MethodLinkSpeciesSet methodLinkSpeciesSet = null;
    Iterator alignList = aligns.iterator();
    GenomicAlign align;
    GenomicAlignBlock alignBlock;
    DnaFragment frag;
    
    while(alignList.hasNext()){
      align = (GenomicAlign)alignList.next();
      methodLinkSpeciesSet = (MethodLinkSpeciesSet)mlSSHash.get(new Long(align.getMethodLinkSpeciesSetInternalId()));
      if(methodLinkSpeciesSet == null){
        // System.out.println("Fetching mlss " + align.getMethodLinkSpeciesSetInternalId() + " from db!");
        methodLinkSpeciesSet = methodLinkSpeciesSetAdaptor.fetch(align.getMethodLinkSpeciesSetInternalId());
        if(methodLinkSpeciesSet == null){
          throw new AdaptorException(
            "Could not find MethodLinkSpeciesSet with ID: "+align.getMethodLinkSpeciesSetInternalId()+
            " attached to genomic align with id: "+align.getInternalID()
          );
        }
        mlSSHash.put(new Long(align.getMethodLinkSpeciesSetInternalId()), methodLinkSpeciesSet);
      }
      align.setMethodLinkSpeciesSet(methodLinkSpeciesSet);
      
      alignBlock = 
        (GenomicAlignBlock)blockHash.get(
          new Long(align.getGenomicAlignBlockInternalID())
        );
      
      if(alignBlock == null){
        // System.out.println("Fetching block " + align.getGenomicAlignBlockInternalID() + " from db!");
        alignBlock = genomicAlignBlockAdaptor.fetch(align.getGenomicAlignBlockInternalID());
        if(alignBlock == null){
          throw new AdaptorException(
            "Could not find GenomicAlignBlock with ID: "+align.getGenomicAlignBlockInternalID()+
            " attached to genomic align with id: "+align.getInternalID()
          );
        }
        blockHash.put(new Long(alignBlock.getInternalID()), alignBlock);
      }
      align.setGenomicAlignBlock(alignBlock);

      frag = 
        (DnaFragment)fragHash.get(
          new Long(align.getDnaFragmentId())
        );
      
      if(frag == null){
        frag = fragAdaptor.fetch(align.getDnaFragmentId());
        if(frag == null){
          throw new AdaptorException(
            "Could not find DnaFrag with ID: "+align.getDnaFragmentId()+
            " attached to genomic align with id: "+align.getInternalID()
          );
        }
        fragHash.put(new Long(frag.getInternalID()), frag);
      }
      align.setDnaFragment(frag);
    }
  }

  private void inflateGenomicAligns(List aligns) throws AdaptorException{
    inflateGenomicAlignsWithBlockCache(aligns,new HashMap());
  }
  
  /**
   * Fetch all genomic aligns matching the parameters.
   *
   * @param dnaFragment the input dnaFragment (implicitly
   * carrying the concensus genomeDB and the targetSpecies)
   * @param targetGenome genome to find alignments in
   * @param start bounded start coord
   * @param end bounded end coord
   * @param methodLinkType input method link type. if null, fetch regardless of method link type.
  **/
  public List fetch(
    DnaFragment dnaFragment, //implicit source species
    GenomeDB targetGenome, 
    int start,
    int end,
    String methodLinkType
  ) throws AdaptorException {

    StringBuffer select = new StringBuffer();
    StringBuffer whereClause = null;
    List arguments = new ArrayList();
    GenomeDBAdaptor genomeDBAdaptor = (GenomeDBAdaptor)getDriver().getAdaptor(GenomeDBAdaptor.TYPE);
    MethodLinkAdaptor methodLinkAdaptor = (MethodLinkAdaptor)getDriver().getAdaptor(MethodLinkAdaptor.TYPE);
    MethodLinkSpeciesSetAdaptor methodLinkSpeciesSetAdaptor = 
        (MethodLinkSpeciesSetAdaptor)getDriver().getAdaptor(MethodLinkSpeciesSetAdaptor.TYPE);
    MethodLink methodLink = null;
    GenomeDB[] genomes = null;
    List methodLinkSpeciesSets = null;
    MethodLinkSpeciesSet methodLinkSpeciesSet;
    int lowerBound;
    PreparedStatement statement;
    List returnList = null;
    Connection connection = null;

    if(dnaFragment == null){
      throw new AdaptorException("Must supply dnaFragment for Genomic Align query");
    }//end if

    if(targetGenome == null){
      throw new AdaptorException("Must supply targetGenome for Genomic Align query");
    }
    
    if(methodLinkType == null){
      throw new AdaptorException("Must supply method link type for Genomic Align query");
    }
    
    methodLink = methodLinkAdaptor.fetch(methodLinkType);

    if(methodLink == null){
      throw new AdaptorException("No method link found with type: "+methodLinkType);
    }
    
    genomes = new GenomeDB[2];
    genomes[0] = dnaFragment.getGenomeDB();
    genomes[1] = targetGenome;
    
    //Get the relevant methodlinkspecies-set based on the input genome pair.
    methodLinkSpeciesSets = methodLinkSpeciesSetAdaptor.fetch(methodLink, genomes);
    
    if(methodLinkSpeciesSets.size() > 1){
      throw new AdaptorException(
        "More than one MethodLinkSpeciesSet returned for the "+
        "combination of MethodLinkType and GenomeDBs"
      );
    }
    
    methodLinkSpeciesSet = (MethodLinkSpeciesSet)methodLinkSpeciesSets.get(0);
    
    whereClause = addEqualsClause(DNAFRAG_ID, new StringBuffer());
    arguments.add(new Long(dnaFragment.getInternalID()));
      
    addEqualsClause(METHOD_LINK_SPECIES_SET_ID, whereClause);
    arguments.add(new Long(methodLinkSpeciesSet.getInternalID()));
      
    if(start > 0 && end > 0){
      lowerBound = start - DEFAULT_MAX_ALIGNMENT;
      /*
         $sql .= ( " AND gab.consensus_start <= $end
                   AND gab.consensus_start >= $lower_bound
                   AND gab.consensus_end >= $start" );
      */
      addLEClause(DNAFRAG_START, whereClause);
      arguments.add(new Integer(end));

      addGEClause(DNAFRAG_START, whereClause);
      arguments.add(new Integer(lowerBound));

      addGEClause(DNAFRAG_END, whereClause);
      arguments.add(new Integer(start));
    }//end if

    try{
      connection = getConnection();
      statement = prepareSelectWithWhereClause(connection, whereClause.toString());
      returnList = executeStatementAndConvertResultToPersistent(statement, arguments);
      for(int i=0; i< returnList.size(); i++){
        GenomicAlign align = (GenomicAlign)returnList.get(i);
        align.setDnaFragment(dnaFragment);
        align.setMethodLinkSpeciesSet(methodLinkSpeciesSet);
      }
    }finally{
      close(connection);
    }

    //inflateGenomicAlignBlocks(returnList, false);//DONT invert the genomic align information!

    return returnList;
  }//end fetch
      
  protected String getTableName(){
    return TABLE_NAME;
  }//end getTableName
  
  protected PersistentImpl createNewObject() {
    return (PersistentImpl)getFactory().createGenomicAlign();
  }//end createNewObject() 
  
  /**
   * Populate object attributes using the columns map.
   * @param columns Hashmap of column names and values. 
   * @param object PersistentImpl passed in to have its attributes populated by these values.
  **/
  protected void mapColumnsToObject(HashMap columns, Persistent object) {
    GenomicAlign genomicAlign = (GenomicAlign)object;
    genomicAlign.setDnaFragmentId(((Long)columns.get(DNAFRAG_ID)).longValue());
    genomicAlign.setStart(((Integer)columns.get(DNAFRAG_START)).intValue());
    genomicAlign.setEnd(((Integer)columns.get(DNAFRAG_END)).intValue());
    genomicAlign.setStrand(((Integer)columns.get(DNAFRAG_STRAND)).intValue());
    genomicAlign.setCigarString((String)columns.get(CIGAR_LINE));
    genomicAlign.setMethodLinkSpeciesSetInternalId(((Long)columns.get(METHOD_LINK_SPECIES_SET_ID)).longValue());
    genomicAlign.setGenomicAlignBlockInternalID(((java.math.BigInteger)columns.get(GENOMIC_ALIGN_BLOCK_ID)).longValue());
    genomicAlign.setInternalID(((java.math.BigInteger)columns.get(GENOMIC_ALIGN_ID)).longValue());
  }//end mapColumnsToObject
  

  protected HashMap mapObjectToColumns(Persistent object) {
    HashMap columns = new HashMap();
    GenomicAlign genomicAlign = (GenomicAlign)object;
    
    columns.put(DNAFRAG_ID, new Long(genomicAlign.getDnaFragmentId()));
    columns.put(DNAFRAG_START, new Integer(genomicAlign.getStart()));
    columns.put(DNAFRAG_END, new Integer(genomicAlign.getEnd()));
    columns.put(DNAFRAG_STRAND, new Integer(genomicAlign.getStrand()));
    columns.put(CIGAR_LINE, genomicAlign.getCigarString());
    columns.put(METHOD_LINK_SPECIES_SET_ID, new Long(genomicAlign.getMethodLinkSpeciesSetInternalId()));
    return columns;
  }//end mapObjectToColumns
  
  public HashMap getLogicalKeyPairs(Persistent genomeDB) throws AdaptorException{
    HashMap logicalKeyPairs = new HashMap();
    return logicalKeyPairs;
  }//end getLogicalKeyPairs
  
  public void validate(Persistent object) throws AdaptorException{
  }//end validate
}
