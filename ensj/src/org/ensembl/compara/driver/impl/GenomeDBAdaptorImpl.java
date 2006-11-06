package org.ensembl.compara.driver.impl;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.compara.driver.GenomeDBAdaptor;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;


/**
 * Fetches GenomeDB objects.
 * @author <a href="maito:vvi@sanger.ac.uk">Vivek Iyer</a>
**/
public class GenomeDBAdaptorImpl extends ComparaBaseAdaptor implements GenomeDBAdaptor{
  
  private HashSet consensusToQueryCrossReferenceList = new HashSet();
  private HashSet queryCrossReferenceList = new HashSet();
  private HashMap genomeDBCacheByID = new HashMap();
  private HashMap genomeDBCacheByNameAssembly = new HashMap();
  private boolean cacheFilled;  

  public static String TABLE_NAME = "genome_db";
  
  public static String GENOME_DB_ID = TABLE_NAME+"."+"genome_db_id";
  public static String NAME = TABLE_NAME+"."+"name";
  public static String ASSEMBLY = TABLE_NAME+"."+"assembly";
  public static String ASSEMBLY_DEFAULT = TABLE_NAME+"."+"assembly_default";
  public static String TAXON_ID = TABLE_NAME+"."+"taxon_id";
  public static String LOCATOR = TABLE_NAME+"."+"locator";
  public static String GENE_BUILD = TABLE_NAME+"."+"genebuild";
  

  public GenomeDBAdaptorImpl(ComparaDriverImpl driver) {
    super(driver);
  }//end MySQLAlignAdaptor

  public String getType(){
    return TYPE;
  }//end getType

  public int store(GenomeDB genomeDB) throws AdaptorException{
    super.store(genomeDB);
    return 0;
  }
  
  /**
   * In order to avoid duplicated entries during a store, we have to provide the
   * logical key of an object about to be stored - this method accepts an input
   * object and returns a Hash of key/value pairs which are the object's logical key
  **/
  public HashMap getLogicalKeyPairs(Persistent genomeDB) throws AdaptorException{
    HashMap logicalKeyPairs = new HashMap();
    logicalKeyPairs.put(NAME, ((GenomeDB)genomeDB).getName());
    logicalKeyPairs.put(ASSEMBLY, ((GenomeDB)genomeDB).getAssembly());
    return logicalKeyPairs;
  }//end getLogicalKeyPairs
  
  public GenomeDB fetch( long internalID ) throws AdaptorException {
    Long id = new Long(internalID);
    
    if(internalID <= 0){
      throw new AdaptorException("Positive internal ID must be provided ");
    }//end if
    
    if(!isCacheFilled()){
      fillCache();
    }//end if

    if(getGenomeDBCacheByID().containsKey(id)){
      return (GenomeDB)getGenomeDBCacheByID().get(id);
    } else {
      throw new AdaptorException(
        "Requested species id " + id + " not in GenomeDB nameCache"
      );
    }//end if
  }//end 
  
  /**
   * Fetch genome db by species name
  **/
  public GenomeDB fetch(String name, String assembly) throws AdaptorException {
    
    if(!isCacheFilled()){
      fillCache();
    }//end if

    if (getGenomeDBCacheByNameAssembly().containsKey(name+assembly)) {
      return (GenomeDB)getGenomeDBCacheByNameAssembly().get(name+assembly);
    } else {
      throw new AdaptorException(
        "Requested species name / assembly " + name + 
        "/"+ assembly+ " not in GenomeDB nameCache"
      );
    }//end if
    
  }//end fetch

  /**
   * Return the genomedb with the input species name, with its
   * assembly marked as 'default': if you can't find such a species,
   * throw an exception
  **/
  public GenomeDB fetch(String speciesName) throws AdaptorException {
    int countOfInstance = 0;
    
    if(!isCacheFilled()){
      fillCache();
    }//end if
    
    String matchName = speciesName+"DEFAULT";
    GenomeDB returnValue = (GenomeDB)getGenomeDBCacheByNameAssembly().get(matchName);
    if(returnValue != null){
      return returnValue;
    }else{
      throw new AdaptorException(
        "Either no genome db with name: "+speciesName+
        " exists, or there is more than one assembly for the species"
      );
    }
  }//end fetch
  
  private void fillCache() throws AdaptorException{
    String sql = null;
    String consensusId;
    String queryId;
    GenomeDB genomeDB = null;

    ResultSet resultSet;
    Iterator genomeDBList = null;
    java.sql.Connection connection = null;

    try {

      //connection = getConnection();
        
      getLogger().fine("filling query/consensus caches for genomedbadapter: "+hashCode());
/*
      sql = 
        "SELECT consensus_genome_db_id, query_genome_db_id "+
        "FROM genomic_align_genome";

      resultSet = connection.createStatement().executeQuery(sql);

      while (resultSet.next()) {
        consensusId = resultSet.getString(1);
        queryId = resultSet.getString(2);

        getConsensusToQueryCrossReferenceList().add(
          String.valueOf(consensusId)+"-"+String.valueOf(queryId)
        );
      }//end while
*/  
      genomeDBList = fetch().iterator();
      
      while (genomeDBList.hasNext()) {
        genomeDB = (GenomeDB)genomeDBList.next();
        getGenomeDBCacheByID().put(new Long(genomeDB.getInternalID()), genomeDB);
        getGenomeDBCacheByNameAssembly().put(genomeDB.getName()+genomeDB.getAssembly(), genomeDB);
        if(genomeDB.isDefaultAssembly()){
          getGenomeDBCacheByNameAssembly().put(genomeDB.getName()+"DEFAULT", genomeDB);
        }
      }//end while
      
    }finally{
      //close(connection);
    }//end try
    
    setCacheFilled(true);
    getLogger().fine("Finished filling genome db cache");
  }
  
  public boolean firstArgumentIsKnownConsensusAndSecondIsKnowQuery(GenomeDB consensusDB, GenomeDB queryDB){
    String key = 
      String.valueOf(consensusDB.getInternalID())+"-"+
      String.valueOf(queryDB.getInternalID());
    
    return getConsensusToQueryCrossReferenceList().contains(key);
  }//end firstArgumentIsKnownConsensusAndSecondIsKnowQuery

  private HashMap getGenomeDBCacheByID(){
    return genomeDBCacheByID;
  }//end getGenomeDBCacheByID
  
  private HashMap getGenomeDBCacheByNameAssembly(){
    return genomeDBCacheByNameAssembly;
  }//end getGenomeDBCacheByNameAssembly
  
  private HashSet getConsensusToQueryCrossReferenceList(){
    return consensusToQueryCrossReferenceList;
  }//end getConsensusToQueryCrossReferenceList

  private boolean isCacheFilled(){
    return cacheFilled;
  }//end isCacheFilled
  
  private void setCacheFilled(boolean newValue){
    cacheFilled = newValue;
  }//end setCacheFilled
  
  protected String getTableName(){
    return TABLE_NAME;
  }//end getTableName
  
  protected PersistentImpl createNewObject() {
    return (PersistentImpl)getFactory().createGenomeDB();
  }//end createNewObject() 
  
  /**
   * Populate object attributes using the columns map.
   * @param columns Hashmap of column names and values. 
   * @param object PersistentImpl passed in to have its attributes populated by these values.
  **/
  protected void mapColumnsToObject(HashMap columns, Persistent object) {
    GenomeDB genomeDB = (GenomeDB)object;
    genomeDB.setInternalID(((Long)columns.get(GENOME_DB_ID)).intValue());
    genomeDB.setTaxonId(((Long)columns.get(TAXON_ID)).intValue());
    genomeDB.setName((String)columns.get(NAME));
    genomeDB.setAssembly((String)columns.get(ASSEMBLY));
    genomeDB.setLocator((String)columns.get(LOCATOR));
    genomeDB.setGeneBuild((String)columns.get(GENE_BUILD));
    genomeDB.setDefaultAssembly(((Boolean)columns.get(ASSEMBLY_DEFAULT)).booleanValue());
  }//end mapColumnsToObject
  
  /**
   * Input; genomeDB with populated attributes. Output - hashmap of columns
   * and their values appropriate for an insert/update.
  **/
  protected HashMap mapObjectToColumns(Persistent object) {
    HashMap columns = new HashMap();
    GenomeDB genomeDB = (GenomeDB)object;
    columns.put(GENOME_DB_ID, new Long(genomeDB.getInternalID()));
    columns.put(TAXON_ID, new Integer(genomeDB.getTaxonId()));
    columns.put(NAME, genomeDB.getName());
    columns.put(ASSEMBLY, genomeDB.getAssembly());
    return columns;
  }//end mapObjectToColumns
  
  public void validate(Persistent object) throws AdaptorException{
    GenomeDB genomeDB = (GenomeDB)object;
    
    if(genomeDB.getInternalID() <= 0){
      throw new AdaptorException("Attempt to store genomeDB "+genomeDB.getName()+" with missing id");
    }//end if
    
    if(genomeDB.getName() == null){
      throw new AdaptorException("Attempt to store genomeDB "+genomeDB.getInternalID()+" with missing name");
    }//end if
    
    if(genomeDB.getAssembly() == null){
      throw new AdaptorException("Attempt to store genomeDB "+genomeDB.getInternalID()+" with missing assembly");
    }//end if
  }//end validate

}
