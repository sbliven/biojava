package org.ensembl.compara.driver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ensembl.compara.datamodel.Homology;
import org.ensembl.compara.datamodel.Member;
import org.ensembl.compara.datamodel.impl.HomologyImpl;
import org.ensembl.compara.driver.HomologyAdaptor;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.util.NotImplementedYetException;


/**
 * Fetches gene-level homology as feature pairs from the compara databases.
 * @author <a href="maito:vvi@sanger.ac.uk">Vivek Iyer</a>
**/
public class 
  HomologyAdaptorImpl 
extends 
  ComparaBaseAdaptor
implements 
  HomologyAdaptor
{
  
  public static String TABLE_NAME = "homology";
  public static String HOMOLOGY_ID = TABLE_NAME+"."+"homology_id";
  public static String STABLE_ID = TABLE_NAME+"."+"stable_id";
  public static String DESCRIPTION = TABLE_NAME+"."+"stable_id";
  public static String METHOD_LINK_SPECIES_SET_ID = TABLE_NAME+"."+"method_link_species_set_id";
  public static String SUBTYPE = TABLE_NAME+"."+"subtype";
  public static String DN = TABLE_NAME+"."+"dn";
  public static String DS = TABLE_NAME+"."+"ds";
  public static String N = TABLE_NAME+"."+"n";
  public static String S = TABLE_NAME+"."+"s";
  public static String LNL = TABLE_NAME+"."+"lnl";
  public static String THRESHOLD_ON_DS = TABLE_NAME+"."+"threshold_on_ds";  
        
  public HomologyAdaptorImpl(ComparaDriverImpl driver) {
    super(driver);
  }//end HomologyAdaptorImpl

  public String getType(){
    return TYPE;
  }
  
  protected void initialise() throws AdaptorException{
  }//end configure.
    
  public void store(Homology feature ) throws  AdaptorException {
    throw new NotImplementedYetException();
  }//end store

  public Homology fetch( long internalID ) throws AdaptorException {
    return (Homology)super.fetch(new Long(internalID));
  }//end fetch
  
  public List fetch(Member member) throws AdaptorException{
    long memberId = member.getInternalID();
    
    //select from homology join homology_member on homology_id where member_id = ?
    
    StringBuffer select = 
      createJoinedSelect(
        new String[]{TABLE_NAME, MemberAdaptorImpl.HOMOLOGY_TABLE_NAME},
        new String[]{HOMOLOGY_ID, MemberAdaptorImpl.HOMOLOGY_MEMBER_HOMOLOGY_ID}
      );
    
    addEqualsClause(MemberAdaptorImpl.MEMBER_ID, select);
    
    List arguments = new ArrayList();
    arguments.add(new Long(memberId));
    Connection connection = getConnection();
    List results = null;
    try{
      PreparedStatement statement = prepareStatement(connection, select.toString());
      results = executeStatementAndConvertResultToPersistent(statement, arguments);
    }finally{
      close(connection);
    }
    return results;
  }
  
  protected  String getTableName(){
    return TABLE_NAME;
  }
  
  protected HashMap mapObjectToColumns(Persistent object){
    if(true){
      throw new IllegalStateException("This adaptor should not be writing this table");
    }
    return null;
  }

  /*
+----------------------------+------------------+
| Field                      | Type             |
+----------------------------+------------------+
| homology_id                | int(10) unsigned |
| stable_id                  | varchar(40)      |
| method_link_species_set_id | int(10) unsigned |
| description                | varchar(40)      |
| subtype                    | varchar(40)      |
| dn                         | float(10,5)      |
| ds                         | float(10,5)      |
| n                          | float(10,1)      |
| s                          | float(10,1)      |
| lnl                        | float(10,3)      |
| threshold_on_ds            | float(10,5)      |
+----------------------------+------------------+
   */
  protected void mapColumnsToObject(HashMap columns, Persistent object){
    Homology homology = (Homology)object;
    homology.setInternalID(((Long)columns.get(HOMOLOGY_ID)).longValue());
    homology.setStableId((String)columns.get(STABLE_ID));
    homology.setMethodLinkSpeciesSetId(((Long)columns.get(METHOD_LINK_SPECIES_SET_ID)).longValue());
    homology.setDescription((String)columns.get(DESCRIPTION));
    homology.setSubtype((String)columns.get(SUBTYPE));
    homology.setDn(((Double)columns.get(DN)).doubleValue());
    homology.setDs(((Double)columns.get(DS)).doubleValue());
    homology.setLnl(((Double)columns.get(LNL)).doubleValue());
    homology.setThresholdOnDs(((Double)columns.get(THRESHOLD_ON_DS)).doubleValue());
  }
  
  protected PersistentImpl createNewObject(){
    return (HomologyImpl)getFactory().createHomology();
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
}
