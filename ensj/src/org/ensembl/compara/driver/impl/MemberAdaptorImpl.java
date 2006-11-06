package org.ensembl.compara.driver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ensembl.compara.datamodel.Member;
import org.ensembl.compara.driver.MemberAdaptor;
import org.ensembl.datamodel.Feature;
import org.ensembl.datamodel.FeaturePair;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.util.NotImplementedYetException;



/**
 * Fetches gene-level homology as feature pairs from the compara databases.
 * @author <a href="maito:vvi@sanger.ac.uk">Vivek Iyer</a>
**/
public class 
  MemberAdaptorImpl 
extends 
  ComparaBaseAdaptor
implements 
  MemberAdaptor
{
  
  public static String TABLE_NAME = "member";
  public static String HOMOLOGY_TABLE_NAME = "homology_member";
  public static String FAMILY_TABLE_NAME = "family_member";
  public static String DOMAIN_TABLE_NAME = "domain_member";
  
  //common member fields
  public static String MEMBER_ID = TABLE_NAME+"."+"member_id";
  public static String VERSION = TABLE_NAME+"."+"version";
  public static String STABLE_ID = TABLE_NAME+"."+"stable_id";
  public static String SOURCE_NAME = TABLE_NAME+"."+"source_name";
  public static String TAXON_ID = TABLE_NAME+"."+"taxon_id";
  public static String GENOME_DB_ID = TABLE_NAME+"."+"genome_db_id";
  public static String SEQUENCE_ID = TABLE_NAME+"."+"sequence_id";
  public static String GENE_MEMBER_ID = TABLE_NAME+"."+"description";
  public static String CHR_NAME = TABLE_NAME+"."+"chr_name";
  public static String CHR_START = TABLE_NAME+"."+"chr_start";
  public static String CHR_END = TABLE_NAME+"."+"chr_end";
  public static String CHR_STRAND = TABLE_NAME+"."+"chr_strand";

  //homology_member fields
  public static String HOMOLOGY_MEMBER_HOMOLOGY_ID = HOMOLOGY_TABLE_NAME+"."+"homology_id";
  public static String HOMOLOGY_MEMBER_MEMBER_ID = HOMOLOGY_TABLE_NAME+"."+"member_id";
  public static String HOMOLOGY_MEMBER_PEPTIDE_MEMBER_ID = HOMOLOGY_TABLE_NAME+"."+"peptide_member_id";
  public static String HOMOLOGY_MEMBER_PEPTIDE_ALIGN_FEATURE_ID = HOMOLOGY_TABLE_NAME+"."+"peptide_align_feature_id";
  public static String HOMOLOGY_MEMBER_CIGAR_LINE = HOMOLOGY_TABLE_NAME+"."+"cigar_line";
  public static String HOMOLOGY_MEMBER_CIGAR_START = HOMOLOGY_TABLE_NAME+"."+"cigar_start";
  public static String HOMOLOGY_MEMBER_CIGAR_END = HOMOLOGY_TABLE_NAME+"."+"cigar_end";
  public static String HOMOLOGY_MEMBER_PERC_COV = HOMOLOGY_TABLE_NAME+"."+"perc_cov";
  public static String HOMOLOGY_MEMBER_PERC_ID = HOMOLOGY_TABLE_NAME+"."+"perc_id";
  public static String HOMOLOGY_MEMBER_PERC_POS = HOMOLOGY_TABLE_NAME+"."+"perc_pos";
  
  //family_member fields
  public static String FAMILY_MEMBER_FAMILY_ID = FAMILY_TABLE_NAME+"."+"family_id";
  public static String FAMILY_MEMBER_MEMBER_ID = FAMILY_TABLE_NAME+"."+"member_id";
  public static String FAMILY_MEMBER_CIGAR_LINE = FAMILY_TABLE_NAME+"."+"cigar_line";

  //domain_member fields
  public static String DOMAIN_MEMBER_DOMAIN_ID = DOMAIN_TABLE_NAME+"."+"domain_id";
  public static String DOMAIN_MEMBER_MEMBER_ID = DOMAIN_TABLE_NAME+"."+"member_id";
  public static String DOMAIN_MEMBER_MEMBER_START = DOMAIN_TABLE_NAME+"."+"member_start";
  public static String DOMAIN_MEMBER_MEMBER_END = DOMAIN_TABLE_NAME+"."+"member_end";
    
  public static String 
    FETCH_HOMOLOGIES_BY_QUERY_SPECIES_LOCATION_AND_HIT_SPECIES =
      "select "+
      "  member1.member_id, "+
      "  member1.chr_start, member1.chr_end, member1.chr_name, member1.stable_id, "+
      "  member2.chr_start, member2.chr_end, member2.chr_name, member2.stable_id "+
      "from "+
      "  genome_db genome_db1, "+
      "  member member1, "+
      "  homology_member homology_member1, "+
      "  homology_member homology_member2, "+
      "  member member2, "+
      "  genome_db genome_db2 "+
      "where  "+
      "  genome_db1.name = ? and "+ //1 - query species name
      "  genome_db1.genome_db_id = member1.genome_db_id and "+
      "  member1.chr_name = ? and "+ //2 - query chr name
      "  member1.chr_start >= ? and "+ //3 - query chr start
      "  member1.chr_end <= ? and "+ //4 - query chr end
      "  member1.member_id = homology_member1.member_id and "+
      "  homology_member1.homology_id = homology_member2.homology_id and "+
      "  homology_member2.member_id = member2.member_id and "+
      "  member2.member_id != member1.member_id and"+
      "  member2.genome_db_id = genome_db2.genome_db_id and "+
      "  genome_db2.name = ? "+ //5 - hit species name
      "group by member2.member_id ";

  public static String 
    FETCH_HOMOLOGIES_BY_QUERY_SPECIES_LOCATION_AND_HIT_SPECIES_LOCATION = 
      "select "+
      "  member1.member_id, "+
      "  member1.chr_start, member1.chr_end, member1.chr_name, member1.stable_id, "+
      "  member2.chr_start, member2.chr_end, member2.chr_name, member2.stable_id "+
      "from "+
      "  genome_db genome_db1, "+
      "  member member1, "+
      "  homology_member homology_member1, "+
      "  homology_member homology_member2, "+
      "  member member2, "+
      "  genome_db genome_db2 "+
      "where  "+
      "  genome_db1.name = ? and "+ //1 - query species name
      "  genome_db1.genome_db_id = member1.genome_db_id and "+
      "  member1.chr_name = ? and "+ //2 - query chr name
      "  member1.chr_start >= ? and "+ //3 - query chr start
      "  member1.chr_end <= ? and "+ //4 - query chr end
      "  member1.member_id = homology_member1.member_id and "+
      "  homology_member1.homology_id = homology_member2.homology_id and "+
      "  homology_member2.member_id = member2.member_id and "+
      "  member2.member_id != member1.member_id and"+
      "  member2.chr_name = ? and "+ //5 - hit chr name
      "  member2.chr_start >= ? and "+ //6 - hit chr start
      "  member2.chr_end <= ? and "+ //7 - hit chr end
      "  member2.genome_db_id = genome_db2.genome_db_id and "+
      "  genome_db2.name = ? "+ //8 - hit species name
      "group by member2.member_id ";
    
  public static String
    FETCH_HOMOLOGIES_BY_QUERY_SPECIES_STABLE_ID_AND_HIT_SPECIES =
      "select "+
      "  member1.member_id, "+
      "  member1.chr_start, member1.chr_end, member1.chr_name, member1.stable_id, "+
      "  member2.chr_start, member2.chr_end, member2.chr_name, member2.stable_id "+
      "from "+
      "  genome_db genome_db1, "+
      "  member member1, "+
      "  homology_member homology_member1, "+
      "  homology_member homology_member2, "+
      "  member member2, "+
      "  genome_db genome_db2 "+
      "where  "+
      "  genome_db1.name = ? and "+ //1 - query species name
      "  genome_db1.genome_db_id = member1.genome_db_id and "+
      "  member1.member_id = homology_member1.member_id and "+
      "  homology_member1.homology_id = homology_member2.homology_id and "+
      "  homology_member2.member_id = member2.member_id and "+
      "  member2.member_id != member1.member_id and "+
      "  member2.genome_db_id = genome_db2.genome_db_id and "+
      "  genome_db2.name = ? and ";
    
  public static String 
    FETCH_SYNTENY_REGIONS_BY_CHROMOSOME = 
      "select sr.synteny_region_id, " +
        "df.coord_system_name, " +
        "df.name, " +
        "dfr.dnafrag_start, " +
        "dfr.dnafrag_end, " +
        "df_h.coord_system_name, " +
        "df_h.name, " +
        "dfr_h.dnafrag_start, " +
        "dfr_h.dnafrag_end, " +
        "sr.rel_orientation " +
        "from dnafrag as df, " +
        "dnafrag as df_h, " +
        "dnafrag_region as dfr, " +
        "dnafrag_region as dfr_h, " +
        "genome_db as gd, " +
        "genome_db as gd_h, " +
        "synteny_region as sr " +
        "where gd.name = ? " +
        "and gd.genome_db_id = df.genome_db_id " +
        "and gd_h.name = ? " +
        "and gd_h.genome_db_id = df_h.genome_db_id " +
        "and df.dnafrag_id = dfr.dnafrag_id  " +
        "and df_h.dnafrag_id = dfr_h.dnafrag_id  " +
        "and dfr.synteny_region_id = sr.synteny_region_id  " +
        "and dfr_h.synteny_region_id = sr.synteny_region_id " +
        "and df.name = ? " + 
        "order by df.name, dfr.dnafrag_start";
      
  public MemberAdaptorImpl(ComparaDriverImpl driver) {
    super(driver);
  }//end HomologyAdaptorImpl

  public String getType(){
    return TYPE;
  }
  
  protected void initialise() throws AdaptorException{
  }//end configure.
    
  /**
   * Not implemented yet!
  **/
  public void store( Feature feature ) throws  AdaptorException {
    throw new NotImplementedYetException();
  }//end store

  /**
   * This method makes no sense, and is not implemented.
  **/
  public Member fetch( long internalID ) throws AdaptorException {
    return (Member)super.fetch(new Long(internalID));
  }//end fetch
 
  /**
   * Fetch gene-level homologies given a location on
   * the chromosome of one of the two species. List contains
   * Feature objects.
  **/
  public List fetch (
    String querySpeciesName,
    Location queryLocation,
    String hitSpeciesName
  ) throws AdaptorException{
    Connection conn = null;
    Feature feature;
    List features = new ArrayList();
    StringBuffer sql;
    ResultSet resultSet;

    int chr1_start = queryLocation.getStart();
    int chr1_end= queryLocation.getEnd();
    String chr1= queryLocation.getSeqRegionName();
    PreparedStatement statement = null;
    String statementString;

    statementString = FETCH_HOMOLOGIES_BY_QUERY_SPECIES_LOCATION_AND_HIT_SPECIES;
    Connection connection = null;
    
    try {
      
      connection = getConnection();
      statement = prepareStatement(connection, statementString);
      statement.setString(1, querySpeciesName);
      statement.setString(2, chr1);
      statement.setInt(3, chr1_start);
      statement.setInt(4, chr1_end);
      statement.setString(5, hitSpeciesName);

      getLogger().fine( "Statment\n" + statementString);
      getLogger().fine( "Parameters: "+querySpeciesName+", "+chr1+","+chr1_start+"-"+chr1_end+", "+hitSpeciesName);
      
      resultSet = statement.executeQuery();

      while ( resultSet.next() ) {
        feature = createFeaturePairsFromResultSet(resultSet);
        features.add(feature);
      }//end while

      return features;

    }catch(SQLException exception){
      throw new AdaptorException("Problem during fetch: ", exception);
    }finally{
      close(connection);
    }
  } 
  
  /**
   * This fetch grabs the protein-protein homologies 
   * between two species which map onto the input list
   * of gene stable id's in the querySpecies. 
  **/
  public List fetch (
    String querySpeciesName,
    String[] queryStableIds,
    String hitSpeciesName
  ) throws AdaptorException{
    
    Connection conn = null;
    Feature feature;
    List features = new ArrayList();
    StringBuffer sql;
    ResultSet resultSet;
    PreparedStatement statement = null;
    List arguments = new ArrayList();
    String statementString;

    statementString = FETCH_HOMOLOGIES_BY_QUERY_SPECIES_STABLE_ID_AND_HIT_SPECIES;

    try {

      sql = new StringBuffer();
      String sqlString;

      //get a partial prepared statement from the static string at the top.
      
      sql.append(statementString);
      
      sql.append(" member1.stable_id in ( "); 

      for(int i=0;i<queryStableIds.length; i++){
        sql.append("?");
        if(i<(queryStableIds.length - 1)){
          sql.append(",");
        }//end if
      }//end for

      sql.append(" ) group by member2.member_id ");
        
      sqlString = sql.toString();
      getLogger().fine("Statement \n"+sqlString);
      conn = getConnection();
      statement = prepareStatement(conn, sqlString);

      arguments.add(querySpeciesName);
      arguments.add(hitSpeciesName);
      for(int i=0;i<queryStableIds.length; i++){
        arguments.add(queryStableIds[i]);
      }//end for
      
      getLogger().fine("Parameters: ");
      for(int i=0;i<queryStableIds.length; i++){
        getLogger().fine("\t"+arguments.get(i));
      }//end for
      
      resultSet = executeStatement(statement, arguments);
      
      while ( resultSet.next() ) {
        feature = createFeaturePairsFromResultSet(resultSet);
        features.add(feature);
      }//end while

      return features;

    } catch ( SQLException exception ) {
      throw new AdaptorException(exception.getMessage(), exception);
    } finally {
      close(conn);
    }
  }
  
  /** 
   * Fetch gene-level homologies given locations on 
   * the chromosomes of the two species. List contains 
   * Feature objects.
  **/
  public List fetch(
    String consensusSpeciesName, 
    Location consensusLocation, 
    String querySpeciesName,
    Location queryLocation
  ) throws  AdaptorException{
    Connection conn = null;
    Feature feature;
    List features = new ArrayList();
    StringBuffer sql;
    ResultSet resultSet;
    List arguments = new ArrayList();

    PreparedStatement statement = null;
    String statementString;
    Connection connection = null;

    statementString = FETCH_HOMOLOGIES_BY_QUERY_SPECIES_LOCATION_AND_HIT_SPECIES_LOCATION;

    try {

      connection = getConnection();
      statement = prepareStatement(connection, statementString);
      
      //
      //add arguments in the same order as expected by the prepared statement
      arguments.add(consensusSpeciesName);
      arguments.add(consensusLocation.getSeqRegionName());
      arguments.add(new Integer(consensusLocation.getStart()));
      arguments.add(new Integer(consensusLocation.getEnd()));
      
      arguments.add(queryLocation.getSeqRegionName());
      arguments.add(new Integer(queryLocation.getStart()));
      arguments.add(new Integer(queryLocation.getEnd()));
      arguments.add(querySpeciesName); 
      
      getLogger().fine( "Statement\n" + statementString);
      getLogger().fine( 
        "Parameters: "+
        consensusSpeciesName+","+
        consensusLocation.getStart()+","+
        consensusLocation.getEnd()+","+
        consensusLocation.getSeqRegionName()+","+
        queryLocation.getSeqRegionName()+
        queryLocation.getStart()+","+
        queryLocation.getEnd()+","+
        querySpeciesName
      );
      
      resultSet = executeStatement(statement, arguments);
      
      while ( resultSet.next() ) {
        feature = createFeaturePairsFromResultSet(resultSet);
        features.add(feature);
      }//end while

      return features;

    }catch(SQLException exception){
      throw new AdaptorException(exception.getMessage(), exception);
    }finally{
      close(connection);
    }
  }//end fetch

  private FeaturePair createFeaturePairsFromResultSet(ResultSet resultSet) throws SQLException{
    FeaturePair featurePair = getFactory().createFeaturePair();
    Location queryLocation;
    Location hitLocation;

    int id        = resultSet.getInt(1);

    int start     = resultSet.getInt(2);
    int end       = resultSet.getInt(3);
    String chr    = resultSet.getString(4);
    String name   = resultSet.getString(5);

    int hstart    = resultSet.getInt(6);
    int hend      = resultSet.getInt(7);
    String hchr   = resultSet.getString(8);
    String hid    = resultSet.getString(9);

    featurePair = getFactory().createFeaturePair();
    
    try{
      featurePair.setLocation(
        new Location("chromosome:"+chr+":"+start+"-"+end+":"+1)
      );      
    }catch(java.text.ParseException exception){
      //I find this use of RuntimeException distasteful, but 
      //we don't have a common FatalException subclass of RuntimeException!
      throw new RuntimeException(
        "Fatal problem parsing location: chromosome:"+chr+":"+start+":"+end+":"+1,
        exception
      );
    }    

    featurePair.setInternalID(id);
    featurePair.setDescription(name);
    featurePair.setDisplayName(name);

    featurePair.setHitDescription(hid);
    featurePair.setHitDisplayName(hid);

    try{
      featurePair.setHitLocation(
        new Location("chromosome:"+hchr+":"+hstart+"-"+hend+":"+1)
      );
    }catch(java.text.ParseException exception){
      //I find this use of RuntimeException distasteful, but 
      //we don't have a common FatalException subclass of RuntimeException!
      throw new RuntimeException(
        "Fatal problem parsing location: chromosome:"+chr+":"+hstart+":"+hend+":"+1,
        exception
      );
    }    
    return featurePair;      
  }//end createFeatureFromResultSet
  
  public FeaturePair createChromosomeLevelFeaturePairFromResultSet(ResultSet rs) throws SQLException {

    int id        = rs.getInt(1);
    String type   = rs.getString(2);
    String name   = rs.getString(3);
    int start     = rs.getInt(4);
    int end       = rs.getInt(5);
    String htype  = rs.getString(6);
    String hname  = rs.getString(7);
    int hstart    = rs.getInt(8);
    int hend      = rs.getInt(9);
    int rel_orient= rs.getInt(10);

    FeaturePair featurePair = 
      getFactory().createFeaturePair();

    try{
      featurePair.setLocation(
        new Location("chromosome:"+name+":"+start+"-"+end+":1")
      );
    }catch(java.text.ParseException exception){
      //I find this use of RuntimeException distasteful, but 
      //we don't have a common FatalException subclass of RuntimeException!
      throw new RuntimeException(
        "Fatal problem parsing location: chromosome:"+name+":"+start+":"+end+":"+1,
        exception
      );
    }    
    
    featurePair.setInternalID(id);

    try{
      featurePair.setHitLocation(
        new Location("chromosome:"+hname+":"+hstart+"-"+hend+":"+rel_orient)
      );
    }catch(java.text.ParseException exception){
      //I find this use of RuntimeException distasteful, but 
      //we don't have a common FatalException subclass of RuntimeException!
      throw new RuntimeException(
        "Fatal problem parsing location: chromosome:"+hname+":"+hstart+":"+hend+":"+1,
        exception
      );
    }       
    
    return featurePair;
  }

  /**
   * Fetches chromosome-level homology as a list of FeaturePairs.
  **/
  public List fetch(
    String species1, 
    String species2, 
    String chromosome
  ) throws AdaptorException{

    
    List returnList = new ArrayList();
    ResultSet resultSet;

    Connection connection = null;
    
    try {

      connection = getConnection();
      PreparedStatement statement = prepareStatement(connection, FETCH_SYNTENY_REGIONS_BY_CHROMOSOME);
      
      statement.setString(1, species1);
      statement.setString(2, species2);
      statement.setString(3, chromosome);
      
      getLogger().fine("Statment\n" + FETCH_SYNTENY_REGIONS_BY_CHROMOSOME);
      getLogger().fine("Parameters: "+species1+","+species2+","+chromosome);
      
      resultSet = statement.executeQuery();
      
      while (resultSet.next()) {
        FeaturePair featurePair = 
          (FeaturePair)createChromosomeLevelFeaturePairFromResultSet(resultSet);
        returnList.add(featurePair);
      }//end while

      return returnList;
    } catch (SQLException exception) {
      throw new AdaptorException("Problem retrieving feature pairs", exception);
    } finally{
      close(connection);
    }//end try
  
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
  
  protected void mapColumnsToObject(HashMap columns, Persistent object){
    if(true){
      throw new IllegalStateException("This adaptor should not be reading from this table");
    }
  }
  
  protected PersistentImpl createNewObject(){
    return (PersistentImpl)getFactory().createFeaturePair();
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
