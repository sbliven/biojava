package org.ensembl.compara.driver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.compara.datamodel.MethodLink;
import org.ensembl.compara.datamodel.MethodLinkSpeciesSet;
import org.ensembl.compara.driver.FatalException;
import org.ensembl.compara.driver.GenomeDBAdaptor;
import org.ensembl.compara.driver.MethodLinkAdaptor;
import org.ensembl.compara.driver.MethodLinkSpeciesSetAdaptor;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;


/**
 * Fetches MethodLink objects.
 * @author <a href="maito:vvi@sanger.ac.uk">Vivek Iyer</a>
**/
public class 
  MethodLinkSpeciesSetAdaptorImpl 
extends 
  ComparaBaseAdaptor 
implements 
  MethodLinkSpeciesSetAdaptor
{
 
  private HashMap _methodLinkCache = new HashMap();
  private boolean _cacheFilled;  

  public static String TABLE_NAME = "method_link_species_set";
  public static String TYPE = TABLE_NAME;

  public MethodLinkSpeciesSetAdaptorImpl(ComparaDriverImpl driver) {
    super(driver);
  }//end MySQLAlignAdaptor

  public String getType(){
    return TYPE;
  }//end getType

  public int store(MethodLink link) throws AdaptorException{
    throw new AdaptorException("STORE method not implemented for MethodLinkSpeciesSetAdaptorImpl");
  }
  
  private HashMap getMethodLinkCache(){
    return _methodLinkCache;
  }//end getMethodLinkCache
  
  private void setMethodLinkCache(HashMap value){
    _methodLinkCache = value;
  }//end getMethodLinkCache
  
  private boolean isCacheFilled(){
    return _cacheFilled;
  }//end isCacheFilled
  
  private void setCacheFilled(boolean newValue){
    _cacheFilled = newValue;
  }//end setCacheFilled
  
  protected String getTableName(){
    return TABLE_NAME;
  }//end getTableName
  
 
  /**
   * We override the default fetch() because we have to create the SpeciesSets
   * by reading each row and accumulating all rows with the same method_link_id and
   * different genome_db_ids into the same method_link_species_set object
  **/
  public List fetch() throws AdaptorException{
   if(isCacheFilled()){
     return new ArrayList(getMethodLinkCache().values());
   }
   
   HashMap speciesSets = new HashMap();
   List returnList = new ArrayList();
   ResultSet rawResults;

   String sql = 
    " SELECT method_link_species_set_id, method_link_id, "+
    " genome_db_id "+
    " FROM method_link_species_set mlss, species_set ss where mlss.species_set_id=ss.species_set_id ";

   PreparedStatement fetch_statement = null;
   Connection connection = null;
   
   try{
     connection = getConnection();
     fetch_statement = prepareStatement(connection, sql);
     rawResults = executeStatement(fetch_statement, new ArrayList());
     
     MethodLinkAdaptor methodLinkAdaptor = (MethodLinkAdaptor)getDriver().getAdaptor(MethodLinkAdaptor.TYPE);
     GenomeDBAdaptor genomeDBAdaptor = (GenomeDBAdaptor)getDriver().getAdaptor(GenomeDBAdaptor.TYPE);
     
     while(rawResults.next()){
       long method_link_species_set_id = rawResults.getLong("method_link_species_set_id");
       long method_link_id = rawResults.getLong("method_link_id");
       String idString = String.valueOf(method_link_species_set_id);
       long genome_db_id = rawResults.getLong("genome_db_id");
       MethodLinkSpeciesSet set = (MethodLinkSpeciesSet)speciesSets.get(idString);

       if(set == null){
         set = getFactory().createMethodLinkSpeciesSet();
         set.setInternalID(method_link_species_set_id);
         MethodLink link = methodLinkAdaptor.fetch(method_link_id);
         if(link == null){
           throw new FatalException("Cannot find a method link with internal id: "+method_link_id);
         }
         set.setMethodLink(link);
         set.setSpeciesSet(new HashSet());
         set.setDriver(getDriver());
         speciesSets.put(idString, set);
       }
       
       GenomeDB genomeDB = genomeDBAdaptor.fetch(genome_db_id);
       if(genomeDB == null){
         throw new FatalException("no genome db found for internal id: "+genome_db_id);
       }
       set.getSpeciesSet().add(genomeDB);
       set.getSpeciesSetdbIDs().add(new Long(genomeDB.getInternalID()));
     }

    }catch(SQLException exception){
      throw new AdaptorException("problems fetching all methodLinkSpeciesSets", exception);
    }finally{
      close(connection);
    }
    
    returnList.addAll(speciesSets.values());
    setMethodLinkCache(speciesSets);
    setCacheFilled(true);
    return returnList;
  }
  
  public MethodLinkSpeciesSet fetch(long internalID) throws AdaptorException{
    if(!isCacheFilled()){
      fetch();
    }
    return (MethodLinkSpeciesSet)getMethodLinkCache().get(String.valueOf(internalID));
  }
  
  public HashMap getLogicalKeyPairs(Persistent genomeDB) throws AdaptorException{
    throw new FatalException("This method should not be getting invoked");
  }
  
  protected PersistentImpl createNewObject() {
    throw new FatalException("This method should not be getting invoked");
  }//end createNewObject() 
  
  protected void mapColumnsToObject(HashMap columns, Persistent object) {
    throw new FatalException("This method should not be getting invoked");
  }//end mapColumnsToObject
  
  protected HashMap mapObjectToColumns(Persistent object) {
    throw new FatalException("This method should not be getting invoked");
  }//end mapObjectToColumns
  
  public void validate(Persistent object) throws AdaptorException{
  }
  
  public List fetch(GenomeDB genomeDB) throws AdaptorException {
    ArrayList returnList = new ArrayList();
    Iterator allSets = fetch().iterator();
    while(allSets.hasNext()){
      MethodLinkSpeciesSet set = (MethodLinkSpeciesSet)allSets.next();
      if(set.getSpeciesSet().contains(genomeDB)){
        returnList.add(set);
      }
    }
    return returnList;
  }
  
  public List fetch(MethodLink methodLink) throws AdaptorException {
    ArrayList returnList = new ArrayList();
    Iterator allSets = fetch().iterator();
    while(allSets.hasNext()){
      MethodLinkSpeciesSet set = (MethodLinkSpeciesSet)allSets.next();
      if(set.getMethodLink().getInternalID() == methodLink.getInternalID()){
        returnList.add(set);
      }
    }
    return returnList;
  }
  
  public List fetch(MethodLink methodLink, GenomeDB genomeDB) throws AdaptorException {
    ArrayList returnList = new ArrayList();
    Iterator allSets = fetch().iterator();
    Iterator genomes = null;
    Long genomeDBID = new Long(genomeDB.getInternalID());
    while(allSets.hasNext()){
      MethodLinkSpeciesSet set = (MethodLinkSpeciesSet)allSets.next();
      genomes = set.getSpeciesSet().iterator();
      while(genomes.hasNext())
      
      if(
        set.getMethodLink().equals(methodLink) && 
        set.getSpeciesSetdbIDs().contains(genomeDBID)
      ){
        returnList.add(set);
      }
    }
    return returnList;
  }
  
  public List fetch(MethodLink methodLink, GenomeDB[] genomes) throws AdaptorException {
    ArrayList returnList = new ArrayList();
    Iterator allSets = null;
    Set specifiedGenomedbIDs = new HashSet();

    if(methodLink != null){
      allSets = fetch(methodLink).iterator();
    }else{
      allSets = fetch().iterator();
    }
    
    for(int i=0; i<genomes.length; i++){
      specifiedGenomedbIDs.add(new Long(genomes[i].getInternalID()));
    }
    
    while(allSets.hasNext()){
      MethodLinkSpeciesSet set = (MethodLinkSpeciesSet)allSets.next();
      if(set.getSpeciesSetdbIDs().containsAll(specifiedGenomedbIDs)){
        returnList.add(set);
      }
    }
    
    return returnList;
  }
  
  public int store(MethodLinkSpeciesSet link) throws AdaptorException{
    throw new FatalException("NOT IMPLEMENTED");
  }


}
