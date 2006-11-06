package org.ensembl.compara.driver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ensembl.compara.datamodel.DnaFragment;
import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.compara.datamodel.GenomicAlign;
import org.ensembl.compara.datamodel.GenomicAlignBlock;
import org.ensembl.compara.datamodel.MethodLinkSpeciesSet;
import org.ensembl.compara.driver.DnaFragmentAdaptor;
import org.ensembl.compara.driver.FatalException;
import org.ensembl.compara.driver.GenomeDBAdaptor;
import org.ensembl.compara.driver.GenomicAlignAdaptor;
import org.ensembl.compara.driver.GenomicAlignBlockAdaptor;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.SequenceRegion;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.SequenceRegionAdaptor;
import org.ensembl.util.IDMap;
import org.ensembl.util.LongList;
import org.ensembl.util.LongSet;


/**
 * Fetches GenomicAlignBlocks from the compara database.
 * @author <a href="maito:vvi@sanger.ac.uk">Vivek Iyer</a>
**/
public class 
  GenomicAlignBlockAdaptorImpl
extends 
  ComparaBaseAdaptor
implements 
  GenomicAlignBlockAdaptor
{
  public static String TABLE_NAME = "genomic_align_block";
  public static String GENOMIC_ALIGN_BLOCK_ID = TABLE_NAME+"."+"genomic_align_block_id";
  public static String METHOD_LINK_SPECIES_SET_ID = TABLE_NAME+"."+"method_link_species_set_id";
  public static String SCORE = TABLE_NAME+"."+"score";
  public static String PERC_ID = TABLE_NAME+"."+"perc_id";
  public static String LENGTH = TABLE_NAME+"."+"length";

  public GenomicAlignBlockAdaptorImpl(ComparaDriverImpl driver) {
    super(driver);
  }//end GenomicAlignAdaptorImpl

  public String getType(){
    return TYPE;
  }//end getType
    
  public int store(GenomicAlign genomicAlign) throws  AdaptorException {
    super.store(genomicAlign);
    return 0;
  }//end store

  public GenomicAlignBlock fetch(long internalID ) throws AdaptorException {
    return (GenomicAlignBlock) super.fetch(new Long(internalID));
  }//end fetch
      
  protected String getTableName(){
    return TABLE_NAME;
  }//end getTableName
  
  protected PersistentImpl createNewObject() {
    return (PersistentImpl)getFactory().createGenomicAlignBlock();
  }//end createNewObject() 
  
  /**
   * Populate object attributes using the columns map.
   * @param columns Hashmap of column names and values. 
   * @param object PersistentImpl passed in to have its attributes populated by these values.
  **/
  protected void mapColumnsToObject(HashMap columns, Persistent object) {
    GenomicAlignBlock genomicAlignBlock = (GenomicAlignBlock)object;
    genomicAlignBlock.setInternalID(((java.math.BigInteger)columns.get(GENOMIC_ALIGN_BLOCK_ID)).longValue());
    genomicAlignBlock.setMethodLinkSpeciesSetInternalId(((Long)columns.get(METHOD_LINK_SPECIES_SET_ID)).longValue());
    genomicAlignBlock.setScore(((Double)columns.get(SCORE)).doubleValue());
    genomicAlignBlock.setPercentageID(((Integer)columns.get(PERC_ID)).intValue());
  }//end mapColumnsToObject
  
  /**
   * Input; genomeDB with populated attributes. Output - hashmap of columns
   * and their values appropriate for an insert/update.
  **/
  protected HashMap mapObjectToColumns(Persistent object) {
    HashMap columns = new HashMap();
    GenomicAlignBlock genomicAlign = (GenomicAlignBlock)object;

    return columns;
  }//end mapObjectToColumns
  
  public HashMap getLogicalKeyPairs(Persistent genomeDB) throws AdaptorException{
    HashMap logicalKeyPairs = new HashMap();
    return logicalKeyPairs;
  }//end getLogicalKeyPairs
  
  public void validate(Persistent object) throws AdaptorException{
  }//end validate
  
  
  public List fetch(DnaFragment dnaFrag) throws AdaptorException{
    List returnList = new ArrayList();
    throw new FatalException("NOT IMPLEMENTED");
  }

  public List fetch(long[] blockIDs) throws AdaptorException{
    List returnList = new ArrayList();
    if (blockIDs.length == 0) {
      return returnList;
    }
    
    StringBuffer select = new StringBuffer();
    StringBuffer whereClause = null;
    List arguments = new ArrayList();
    PreparedStatement statement;
    
    Connection connection = getConnection();
    StringBuffer sql;
    
    whereClause = new StringBuffer(" where " + GENOMIC_ALIGN_BLOCK_ID + " in (");
    whereClause.append(new LongList(blockIDs).toCommaSeparatedString());
    whereClause.append(")");

    sql = createSelectUpToWhere().append(whereClause.toString());


    try{
      statement = prepareStatement(connection, sql.toString());
      returnList = executeStatementAndConvertResultToPersistent(statement, arguments);
    }finally{
      close(connection);
    }

    return returnList;
  }
  
  public List fetch(MethodLinkSpeciesSet methLinkSpecSet, DnaFragment dnaFrag, int start, int end) throws AdaptorException{
    Map blockID2alignID = new HashMap();
    long genomic_align_id;
    long genomic_align_block_id;
    GenomicAlignAdaptor genomicAlignAdaptor = (GenomicAlignAdaptor)getDriver().getAdaptor(GenomicAlignAdaptor.TYPE);
    List blockList = new ArrayList();
    
    if(methLinkSpecSet == null){
      throw new AdaptorException("Must provide MethodLinkSpeciesSet");
    }
    
    if(dnaFrag == null){
      throw new AdaptorException("Must provide DnaFragment");
    }
    
    String sql = 
      "SELECT genomic_align_id, genomic_align_block_id FROM genomic_align WHERE method_link_species_set_id = "+
      methLinkSpecSet.getInternalID()+
      " and dnafrag_id = "+
      dnaFrag.getInternalID();
    
    if((start > 0) && (end > 0)){
      sql = sql +
        " and dnafrag_start <= "+end+
        " and dnafrag_end >= "+start+
        " and dnafrag_start >= "+(start-GenomicAlignAdaptorImpl.DEFAULT_MAX_ALIGNMENT);
    }

    Connection connection = null;
    PreparedStatement statement = null;
    try{
      connection = getConnection();
      statement = prepareStatement(connection, sql);
      java.sql.ResultSet results = executeStatement(statement, new ArrayList());

      // Uniquify
      while(results.next()){
        genomic_align_block_id = results.getLong("genomic_align_block_id");
        genomic_align_id = results.getLong("genomic_align_id");
        if(blockID2alignID.get(new Long(genomic_align_block_id)) == null){
          blockID2alignID.put((new Long(genomic_align_block_id)), (new Long(genomic_align_id)));
        }
      }

      // Big (batch) fetch - much faster than individual fetchs
      blockList = fetch(LongSet.to_longArray(blockID2alignID.keySet()));

      // Set reference GenomicAlign id - actual align is set later - faster to do it that way
      Iterator blockIter = blockList.iterator();
      while(blockIter.hasNext()){
        GenomicAlignBlock block = (GenomicAlignBlock) blockIter.next();
        block.setReferenceGenomicAlignInternalID(((Long)blockID2alignID.get(new Long(block.getInternalID()))).longValue());
      }
      
    }catch(java.sql.SQLException exception){
      throw new AdaptorException("Problems executing sql: "+exception.getMessage(), exception);
    }finally{
      close(connection);
    }

    // Fill in the GenomicAligns in all the blocks and sets the ReferenceGenomicAlign 
    // in each block
    IDMap id2block = new IDMap(blockList);
    List aligns = genomicAlignAdaptor.fetchByBlocks(blockList);
    for (int i = 0; i < aligns.size(); i++) {
      GenomicAlign align = (GenomicAlign)aligns.get(i);
      GenomicAlignBlock block = (GenomicAlignBlock)id2block.get(align.getGenomicAlignBlockInternalID());
      block.addGenomicAlign(align);
      if (align.getInternalID() == block.getReferenceGenomicAlignInternalID()) {
        block.setReferenceGenomicAlign(align);
      }
    }
    
    return blockList;
  }
  
  public List fetch(MethodLinkSpeciesSet methLinkSpecSet, Location referenceLocation) throws AdaptorException{
    List returnList = new ArrayList();
    
    //
    //Use the reference Location passed in to go back to the core adaptor's meta container, to 
    //fetch out the species binomial, so we can then fetch out the genomedb of the species.
    SequenceRegion region = referenceLocation.getSequenceRegion();
    org.ensembl.driver.CoreDriver coreDriver = null;
    String speciesBinomial = null;
    String coordSystem = null;
    GenomeDBAdaptor genomeDBAdaptor = null;
    GenomeDB genomeDB = null;
    DnaFragmentAdaptor dnaFragAdaptor = null;
    List dnaFrags;
    DnaFragment dnaFrag = null;
    Iterator blocks;
    GenomicAlignBlock block;
    SequenceRegionAdaptor seqRegionAdaptor = null;
    SequenceRegion topLevelSeqRegion = null;
    Location topLevelLocation = null;

    if(region != null){
      coreDriver = region.getDriver();
    }else{
      throw new AdaptorException("Location passed in must have a SequenceRegion attached");
    }
    
    //
    //CHANGE ME 
    // When I know what the issue is.
    if(!(referenceLocation.getSequenceRegion().getAttributeValue("toplevel") == null)){
      throw new AdaptorException("Must provide a Location corresponding to a toplevel SeqRegion");
    }
    
    if(coreDriver != null){
      speciesBinomial = getSpeciesBinomial(coreDriver);
    }else{
      throw new AdaptorException("SequenceRegion must have a core driver attached");
    }
    
    coordSystem = referenceLocation.getCoordinateSystem().getName();
    
    genomeDBAdaptor = (GenomeDBAdaptor)getDriver().getAdaptor(GenomeDBAdaptor.TYPE);
    
    genomeDB = genomeDBAdaptor.fetch(speciesBinomial, coordSystem);
    
    
    //
    //Take the reference location and project it to the toplevel -- unnecessary, since we are forcing
    //the input location to be toplevel. 
    //
    //Get the dnafrag corresponding to the genome db (above) and the name of each projected segment.
    dnaFragAdaptor = (DnaFragmentAdaptor)getDriver().getAdaptor(DnaFragmentAdaptor.TYPE);
    dnaFrags = dnaFragAdaptor.fetch(genomeDB, coordSystem, referenceLocation.getSeqRegionName());
    if(dnaFrags.size() > 1){
      throw new AdaptorException("More than one DnaFragment corresponding to the seqRegion passed in: "+referenceLocation.getSeqRegionName());
    }
    dnaFrag = (DnaFragment)dnaFrags.get(0);
    
    //
    //FETCH genomic align blocks by methodLinkSpeciesSet dNAFrag, start, end
    blocks = fetch(methLinkSpecSet, dnaFrag, referenceLocation.getStart(), referenceLocation.getEnd()).iterator();
    
    //BUT we do need to locate the Location corresponding to the
    //entire toplevel slice. (So if we are passed in a Location of Chr 7:1Mb-2Mb, we have to produce
    //a Location of the whole of Chr7 (1 - length of Chr7).
    topLevelLocation = new Location(referenceLocation.getCoordinateSystem(), referenceLocation.getSeqRegionName());
    topLevelLocation.setSequenceRegion(referenceLocation.getSequenceRegion());
    
    while(blocks.hasNext()){
      block = (GenomicAlignBlock)blocks.next();
      block.setReferenceSlice(topLevelLocation);
      block.setReferenceSliceStart(block.getReferenceGenomicAlign().getStart());
      block.setReferenceSliceEnd(block.getReferenceGenomicAlign().getEnd());
      returnList.add(block);
    }

    return returnList;
  }
  
  private String getSpeciesBinomial(org.ensembl.driver.CoreDriver coreDriver) throws AdaptorException{
    String sql = "select * from meta where meta_key = 'species.classification' order by meta_key limit 2";
    Connection connection = getConnection();
    PreparedStatement statement = prepareStatement(connection, sql);
    ResultSet set = null;
    String species = null;
    String genus = null;
    int count = 0;
    
    try{
     set = statement.executeQuery();

     if(set.next()){
      species = set.getString(1);
     }else{
       throw new AdaptorException("No species information in meta table");
     }
     
     if(set.next()){
      genus = set.getString(1);
     }else{
       throw new AdaptorException("No genus information in meta table");
     }
     
    }catch(SQLException exception){
      throw new AdaptorException("Unable to find species binomial: "+exception.getMessage(), exception);
    }finally{
      close(connection);
    }
    
    return genus+" "+species;
  }
}
