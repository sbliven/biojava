package org.ensembl.compara.driver.impl;

import java.util.HashMap;
import java.util.Iterator;

import org.ensembl.compara.datamodel.MethodLink;
import org.ensembl.compara.driver.MethodLinkAdaptor;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;


/**
 * Fetches MethodLink objects.
 * @author <a href="maito:vvi@sanger.ac.uk">Vivek Iyer</a>
**/
public class MethodLinkAdaptorImpl extends ComparaBaseAdaptor implements MethodLinkAdaptor{
 
  private HashMap methodLinkCacheByID = new HashMap();
  private HashMap methodLinkCacheByType = new HashMap();
  private boolean cacheFilled;  

  public static String TABLE_NAME = "method_link";
  
  public static String METHOD_LINK_ID = TABLE_NAME+"."+"method_link_id";
  public static String METHOD_LINK_TYPE = TABLE_NAME+"."+"type";
  public static String TYPE = TABLE_NAME;

  public MethodLinkAdaptorImpl(ComparaDriverImpl driver) {
    super(driver);
  }//end MySQLAlignAdaptor

  public String getType(){
    return TYPE;
  }//end getType

  public int store(MethodLink link) throws AdaptorException{
    super.store(link);
    return 0;
  }
  
  /**
   * In order to avoid duplicated entries during a store, we have to provide the
   * logical key of an object about to be stored - this method accepts an input
   * object and returns a Hash of key/value pairs which are the object's logical key
  **/
  public HashMap getLogicalKeyPairs(Persistent methodLink) throws AdaptorException{
    HashMap logicalKeyPairs = new HashMap();
    logicalKeyPairs.put(TYPE, ((MethodLink)methodLink).getType());
    return logicalKeyPairs;
  }//end getLogicalKeyPairs
  
  /**
   * Fetch genome db by species name
  **/
  public MethodLink fetch(String type) throws AdaptorException {
    
    if(!isCacheFilled()){
      fillCache();
    }//end if

    if (getMethodLinkCacheByType().containsKey(type)) {
      return (MethodLink)getMethodLinkCacheByType().get(type);
    } else {
      throw new AdaptorException(
        "Requested type " + type + 
        " not in cache"
      );
    }//end if
    
  }//end fetch

  public MethodLink fetch(long internalID) throws AdaptorException {
    if(!isCacheFilled()){
      fillCache();
    }//end if

    if (getMethodLinkCacheByID().containsKey(String.valueOf(internalID))) {
      return (MethodLink)getMethodLinkCacheByID().get(String.valueOf(internalID));
    } else {
      throw new AdaptorException(
        "Requested type " + internalID + 
        " not in cache"
      );
    }//end if
  }//end fetch

  private void fillCache() throws AdaptorException{
    String sql = null;
    String consensusId;
    String queryId;
    MethodLink link = null;
    Iterator methodLinkList = null;

    
    getLogger().fine("filling cache for MethodLinkAdaptorImpl");
    methodLinkList = fetch().iterator();

    while (methodLinkList.hasNext()) {
      link = (MethodLink)methodLinkList.next();
      getMethodLinkCacheByID().put(String.valueOf(link.getInternalID()), link);
      getMethodLinkCacheByType().put(link.getType(), link);
    }//end while
  
    setCacheFilled(true);
    getLogger().fine("Finished filling cache");
  }
  
  private HashMap getMethodLinkCacheByType(){
    return methodLinkCacheByType;
  }//end getMethodLinkCacheByType
  
  private HashMap getMethodLinkCacheByID(){
    return methodLinkCacheByID;
  }//end getMethodLinkCacheById
  
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
    return (PersistentImpl)getFactory().createMethodLink();
  }//end createNewObject() 
  
  /**
   * Populate object attributes using the columns map.
   * @param columns Hashmap of column names and values. 
   * @param object PersistentImpl passed in to have its attributes populated by these values.
  **/
  protected void mapColumnsToObject(HashMap columns, Persistent object) {
    MethodLink methodLink = (MethodLink)object;
    methodLink.setInternalID(((Long)columns.get(METHOD_LINK_ID)).longValue());
    methodLink.setType(((String)columns.get(METHOD_LINK_TYPE)));
  }//end mapColumnsToObject
  
  /**
   * Input; methodLink with populated attributes. Output - hashmap of columns
   * and their values appropriate for an insert/update.
  **/
  protected HashMap mapObjectToColumns(Persistent object) {
    HashMap columns = new HashMap();
    MethodLink methodLink = (MethodLink)object;
    columns.put(METHOD_LINK_ID, new Long(methodLink.getInternalID()));
    columns.put(METHOD_LINK_TYPE, methodLink.getType());
    return columns;
  }//end mapObjectToColumns
  
  public void validate(Persistent object) throws AdaptorException{
    MethodLink link = (MethodLink)object;
    
    if(link.getInternalID() <= 0){
      throw new AdaptorException("Attempt to store link "+link.getType()+" with missing id");
    }//end if
    
    if(link.getType() == null){
      throw new AdaptorException("Attempt to store link "+link.getInternalID()+" with missing type");
    }//end if
  }//end validate

}
