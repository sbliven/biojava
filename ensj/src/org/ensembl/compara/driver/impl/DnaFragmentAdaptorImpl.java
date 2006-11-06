package org.ensembl.compara.driver.impl;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ensembl.compara.datamodel.DnaFragment;
import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.compara.driver.DnaFragmentAdaptor;
import org.ensembl.compara.driver.GenomeDBAdaptor;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;

/**
 * @author <a href="maito:vvi@sanger.ac.uk">Vivek Iyer</a>
**/
public class DnaFragmentAdaptorImpl extends ComparaBaseAdaptor implements DnaFragmentAdaptor{

  public static String TABLE_NAME = "dnafrag";
  
  public static String DNAFRAG_ID = TABLE_NAME+"."+"dnafrag_id";
  public static String COORD_SYSTEM_NAME = TABLE_NAME+"."+"coord_system_name";
  public static String GENOME_DB_ID = TABLE_NAME+"."+"genome_db_id";
  public static String NAME = TABLE_NAME+"."+"name";
  public static String LENGTH = TABLE_NAME+"."+"length";
  

  public DnaFragmentAdaptorImpl(ComparaDriverImpl driver) {
    super(driver);
  }//end MySQLAlignAdaptor

  public String getType(){
    return TYPE;
  }//end getType

  public int store(DnaFragment object) throws AdaptorException{
    super.store(object);
    return 0;
  }
  
  public DnaFragment fetch( long internalID ) throws AdaptorException {
    Long id = new Long(internalID);
    DnaFragment fragment = (DnaFragment)super.fetch(id);
    inflateDnaFragment(fragment);
    return fragment;
  }//end 
  
  /**
   * Fetch genome db by species name
  **/
  public List fetch(
    GenomeDB genomeDB, 
    String coordSystemName, 
    String name
  )throws AdaptorException{
    StringBuffer whereClause = null;
    PreparedStatement statement = null;
    List arguments = new ArrayList();
    GenomeDBAdaptor genomeDBAdaptor = null;
    List returnList;
    DnaFragment dnaFragment;
    java.sql.Connection connection = getConnection();
    
    if(genomeDB == null){
      throw new AdaptorException("Genome DB must be specified for fetch");
    }//end if
    
    whereClause = addEqualsClause(GENOME_DB_ID, (new StringBuffer()));
    arguments.add(new Long(genomeDB.getInternalID()));

    if(coordSystemName != null){
      whereClause = addEqualsClause(COORD_SYSTEM_NAME, whereClause);
      arguments.add(coordSystemName);
    }
    
      
    if(name != null){
      addEqualsClause(NAME, whereClause);
      arguments.add(name);
    }//end if
    
    try{
      statement = prepareSelectWithWhereClause(connection, whereClause.toString());

      returnList = executeStatementAndConvertResultToPersistent(statement, arguments);

      for(int i=0; i< returnList.size(); i++){
        dnaFragment = (DnaFragment)returnList.get(i);
        dnaFragment.setGenomeDB(genomeDB);
      }//end for
    }finally{
      close(connection);
    }

    return returnList;
  }//end fetch

  /**
   * Input - dnafragment with no genome db object
   * output - dnafragment with genomeDb attached
  **/
  private void inflateDnaFragment(DnaFragment fragment) throws AdaptorException{
    GenomeDBAdaptor genomeDBAdaptor = 
      (GenomeDBAdaptor)getDriver().getAdaptor(GenomeDBAdaptor.TYPE);
    
    if(fragment.getGenomeDB() == null){
      fragment.setGenomeDB(genomeDBAdaptor.fetch(fragment.getGenomeDbInternalId()));
    }//end if
  }
  
  protected String getTableName(){
    return TABLE_NAME;
  }//end getTableName
  
  protected PersistentImpl createNewObject() {
    return (PersistentImpl)getFactory().createDnaFragment();
  }//end createNewObject() 
  
  /**
   * Does NOT gather the GenomeDB object - that's left to the fetch() methods.
  **/
  protected void mapColumnsToObject(HashMap columns, Persistent object) {
    DnaFragment dnaFragment = (DnaFragment)object;
    dnaFragment.setInternalID(((Long)columns.get(DNAFRAG_ID)).longValue());
    dnaFragment.setLength(((Integer)columns.get(LENGTH)).intValue());
    dnaFragment.setGenomeDbInternalId(((Long)columns.get(GENOME_DB_ID)).longValue());
    dnaFragment.setCoordSystemName((String)columns.get(COORD_SYSTEM_NAME));
    dnaFragment.setName((String)columns.get(NAME));
  }//end mapColumnsToObject
  
  /**
   * Input; genomeDB with populated attributes. Output - hashmap of columns
   * and their values appropriate for an insert/update.
  **/
  protected HashMap mapObjectToColumns(Persistent object) {
    HashMap columns = new HashMap();
    DnaFragment dnaFragment = (DnaFragment)object;
    columns.put(DNAFRAG_ID, new Long(dnaFragment.getInternalID()));
    columns.put(COORD_SYSTEM_NAME, new Integer(dnaFragment.getCoordSystemName()));
    columns.put(NAME, dnaFragment.getName());
    columns.put(GENOME_DB_ID, new Long(dnaFragment.getGenomeDbInternalId()));
    return columns;
  }//end mapObjectToColumns
  
  public HashMap getLogicalKeyPairs(Persistent object) throws AdaptorException{
    HashMap logicalKeyPairs = new HashMap();
    DnaFragment dnaFragment = (DnaFragment)object;
    logicalKeyPairs.put(COORD_SYSTEM_NAME, dnaFragment.getCoordSystemName());
    logicalKeyPairs.put(NAME, dnaFragment.getName());
    return logicalKeyPairs;
  }//end getLogicalKeyPairs
  
  public void validate(Persistent object) throws AdaptorException{
    DnaFragment dnaFragment = (DnaFragment)object;
    if(dnaFragment.getInternalID() <= 0){
      throw new AdaptorException("Attempt to store dnaFragment "+dnaFragment.getName()+" with missing id");
    }//end if
    
    if(dnaFragment.getName() == null){
      throw new AdaptorException("Attempt to store dnaFragment "+dnaFragment.getInternalID()+" with missing name");
    }//end if   
    
    if(dnaFragment.getCoordSystemName() == null){
      throw new AdaptorException("Attempt to store dnaFragment "+dnaFragment.getInternalID()+" with missing coord system name");
    }//end if   
    
    if(dnaFragment.getGenomeDB() == null){
      throw new AdaptorException("Attempt to store dnaFragment "+dnaFragment.getName()+" with missing GenomeDB");
    }//end if   
  }//end validate
}//end DnaFragmentAdaptorImpl 
