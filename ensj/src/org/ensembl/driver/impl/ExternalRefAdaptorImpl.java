/*
  Copyright (C) 2003 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later versibon.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.driver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.ExternalDatabase;
import org.ensembl.datamodel.ExternalRef;
import org.ensembl.datamodel.impl.ExternalRefImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.ExternalDatabaseAdaptor;
import org.ensembl.driver.ExternalRefAdaptor;
import org.ensembl.util.JDBCUtil;


public class ExternalRefAdaptorImpl extends BaseAdaptor implements ExternalRefAdaptor {
  
  private static final Logger logger = Logger.getLogger(ExternalRefAdaptorImpl.class.getName());
  
  private final String COLUMNS = "   xf.xref_id,           \n" +
  "   xf.dbprimary_acc,     \n" +
  "   xf.external_db_id,     \n" +
  "   xf.display_label,       \n" +
  "   xf.version,          \n" +
  "   xf.description,       \n" +
  "   ox.ensembl_object_type, \n" +
  "   ox.object_xref_id, \n" +
  "   xf.info_type, \n" +
  "   xf.info_text \n";
  
  
  public ExternalRefAdaptorImpl(CoreDriverImpl driver) {
    super(driver);
  }
  
  public String getType() throws org.ensembl.driver.AdaptorException {
    return TYPE;
  }
  
  
  public List fetch(long internalID) throws AdaptorException {
    
    List result = Collections.EMPTY_LIST;
    
    Connection conn = null;
    try {
      
      conn = getConnection();
      String sql =
      "SELECT " +
      COLUMNS + 
      "FROM                      " +
      "  xref xf LEFT JOIN object_xref ox ON ox.xref_id = xf.xref_id " +
      //"  LEFT JOIN external_synonym es ON xf.xref_id = es.xref_id " +
      "WHERE                     " +
      "  xf.xref_id = ?";
      
      logger.info( JDBCUtil.beautifySQL(sql) );
      
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setLong( 1, internalID );
      result = buildExternalRefs( executeQuery( ps, sql ));
      
    } catch (Exception e) {
      throw new AdaptorException("Failed to fetch externalRef: "
      + internalID, e);
    }finally {
      close( conn );
    }
    
    return result;
  }
  
  /**
   * Fetches ExternalRefs for the item with the specified _type_ and _internalID_.
   * @param ensemblId internalID id of the persistent object to find references for
   * @param type type of object, must be one of ExternalRef.GENE, ExternalRef.TRANSLATION, ExternalRef.TRANSCRIPT or ExternalRef.CLONE_FRAGMENT
   *
   * @return List containing zero or more ExternalRefs for the specified item.
   */
  public List fetch(long ensemblId,int type) throws AdaptorException {
    List externalRefs =null;
    ResultSet rs = null;
    Connection conn = null;
    PreparedStatement ps = null;
    
    String objTypeTable;    // derived from type
    String objTypePkField;  // primary key field to link w/ensembl_id
    String objType;
    
    switch (type) {
      case ExternalRef.GENE:
        objTypeTable = "gene";
        objTypePkField = "gene_id";
        objType        = "Gene";
        break;
      case ExternalRef.TRANSLATION:
        objTypeTable = "translation";
        objTypePkField = "translation_id";
        objType        = "Translation";
        break;
      case ExternalRef.TRANSCRIPT:
        objTypeTable = "transcript";
        objTypePkField = "transcript_id";
        objType        = "Transcript";
        break;
      case ExternalRef.CLONE_FRAGMENT:
        objTypeTable = "contig";
        objTypePkField = "internal_id";
        objType        = "RawContig";
        break;
      default:
        throw new AdaptorException("Type: " + type + " not a valid type of object, must " +
        " be one of ExternalRef.GENE, ExternalRef.TRANSLATION, ExternalRef.TRANSCRIPT or ExternalRef.CLONE_FRAGMENT");
    }
    
    
    try {
      // Retrieve row from database
      conn = getConnection();
      String sql =
      " SELECT " +
      COLUMNS +
      " FROM                   \n" +
      "  xref xf,              \n" +
      "  object_xref      ox,    \n" +
      " "+objTypeTable+ "   ot \n" +
      " WHERE                  \n" +
      "  xf.xref_id = ox.xref_id \n" +
      "  AND                   \n" +
      "  ox.ensembl_id = ot." + objTypePkField+"    \n" +
      "  AND                   \n" +
      "  ox.ensembl_id = ?     \n" +
      "  AND                   \n" +
      "  ox.ensembl_object_type = ?";
      
      
      ps = conn.prepareStatement(sql);
      ps.setLong(1,ensemblId);
      ps.setString(2,objType);
      logger.fine(sql);
      
      rs =  ps.executeQuery();
      externalRefs = buildExternalRefs(rs);
    }catch(SQLException e) {
      throw new AdaptorException("Rethrow + stacktrace", e);
    }finally {
      close( conn );
    }
    return externalRefs;
  }
  
  
  /**
   * Fetches ExternalRefs where name appears as either a primaryID, displayID
   * or synonym.
   * @param name Name to find matching refs for.
   *
   * @return List containing zero or more ExternalRefs with the specified synonym.
   */
  public List fetch(String name) throws org.ensembl.driver.AdaptorException {
    List externalRefs = new ArrayList();
    ResultSet rs = null;
    Connection conn = null;
    PreparedStatement ps = null;
    String sql = null;
    
    try {
      // Retrieve all display ids that match
      conn = getConnection();
      sql =
      " SELECT " +
      COLUMNS +
      " FROM" +
      "    xref xf       \n" +
      "    LEFT JOIN object_xref ox ON ox.xref_id = xf.xref_id\n" +
      " WHERE" +
      "   xf.display_label = ?";
      
      ps = conn.prepareStatement(sql);
      ps.setString(1,name);
      logger.fine(sql);
      
      rs =  executeQuery(ps, sql);
      externalRefs.addAll(buildExternalRefs(rs));
      
      // Retrieve all display ids that match
      sql =
      "SELECT " +
      COLUMNS +
      "FROM                  \n" +
      "  xref xf,             \n" +
      "  object_xref ox \n" +
      "WHERE                 \n" +
      "   ox.xref_id = xf.xref_id \n" +
      "   AND                   \n" +
      "   dbprimary_acc = ?";
      
      ps = conn.prepareStatement(sql);
      ps.setString(1,name);
      logger.fine(sql);
      
      rs =  executeQuery(ps, sql);
      externalRefs.addAll(buildExternalRefs(rs));
      
      
      // Retrieve all display ids that match
      sql =
      "SELECT " +
      COLUMNS +
      "FROM                      \n" +
      "  xref xf,                \n" +
      "  external_synonym es,      \n" +
      "  object_xref ox           \n" +
      "WHERE                     \n" +
      "  ox.xref_id = xf.xref_id  \n" +
      "  AND                     \n" +
      "  xf.xref_id = es.xref_id   \n" +
      "  AND                     \n" +
      "  es.synonym = ?";
      
      ps = conn.prepareStatement(sql);
      ps.setString(1,name);
      logger.fine(sql);
      
      rs =  executeQuery(ps, sql);
      externalRefs.addAll(buildExternalRefs(rs));
      
    }catch(SQLException e) {
    	e.printStackTrace();
      throw new AdaptorException("Rethrow + stacktrace", e);
    }finally {
      close( conn );
    }
    return externalRefs;
  }
  
  private List buildExternalRefs(ResultSet rs) throws SQLException,AdaptorException {
    
    ArrayList externalRefs = new ArrayList();
    
    ExternalDatabaseAdaptor externalDatabaseAdaptor
    = driver.getExternalDatabaseAdaptor();
    
    while(rs.next()) {
      
      ExternalRefImpl xRef = new ExternalRefImpl(driver);
      final long internalID = rs.getLong("xref_id");
      final long dbInternalID = rs.getLong("external_db_id");
      final ExternalDatabase externalDatabase
      = externalDatabaseAdaptor.fetch(dbInternalID);
      
      xRef.setInternalID( internalID );
      xRef.setPrimaryID(rs.getString("dbprimary_acc"));
      xRef.setDisplayID(rs.getString("display_label"));
      xRef.setVersion(rs.getString("version"));
      xRef.setDescription(rs.getString("description"));
      xRef.setExternalDbId( dbInternalID);
      xRef.setExternalDatabase( externalDatabase );
      xRef.setInfoType(rs.getString("info_type"));
      xRef.setInfoText(rs.getString("info_text"));
      xRef.setObjectXrefID(rs.getLong("object_xref_id"));
      
      externalRefs.add(xRef);
    }
    return externalRefs;
  }
  
  
  public ExternalRef fetchCompleteGoLinkageType(ExternalRef xRef) throws AdaptorException {
    
    Connection conn = null;
    
    try {
      
      conn = getConnection();
      String goSQL = "SELECT linkage_type FROM go_xref WHERE object_xref_id = " + xRef.getObjectXrefID();
      ResultSet rs = executeQuery(conn, goSQL);
      if (rs.next()) 
        xRef.setGoLinkageType(rs.getString("linkage_type")); // assumes there are 0 or 1
            
    } catch (SQLException e) {
      throw new AdaptorException(e);
    } finally {
      close(conn);
    }
    return xRef;
  }
  
  
  public ExternalRef fetchCompleteIdentity(ExternalRef xRef) throws AdaptorException {
    
    Connection conn = null;
    try {
    
      conn = getConnection();
      String sql = "SELECT query_identity, target_identity FROM identity_xref WHERE object_xref_id = " + xRef.getObjectXrefID();
      ResultSet rs = executeQuery(conn, sql);
      if (rs.next()) { // assumes there are 0 or 1
        xRef.setTargetIdentity(rs.getInt("target_identity"));
        xRef.setQueryIdentity(rs.getInt("query_identity"));
      }
            
    } catch (SQLException e) {
      throw new AdaptorException(e);
    } finally {
      close(conn);
    }
    return xRef;
  }
  
  public ExternalRef fetchCompleteSynonyms(ExternalRef xRef) throws AdaptorException {
    
    ArrayList synonymns = new ArrayList();
    xRef.setSynonyms(synonymns);
    
    ResultSet rs = null;
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      // Retrieve row from database
      conn = getConnection();
      String sql =
      " select synonym      \n" +
      " from                \n" +
      "   external_synonym es\n" +
      " where               \n" +
      "   es.xref_id = ? ";
      
      ps = conn.prepareStatement(sql);
      ps.setLong(1,xRef.getInternalID());
      logger.fine(sql);
      
      rs =  ps.executeQuery();
      while(rs.next()) {
        synonymns.add( rs.getString(1) );
      }
    }catch(SQLException e) {
      throw new AdaptorException("Rethrow + stacktrace", e);
    }finally {
      close( conn );
    }
    return xRef;
  }
  
  
  public long storeObjectExternalRefLink(long ensemblInternalID, int ensemblType, long externalRefInternalID) throws AdaptorException {
    long linkID = -1;

    Connection conn = null;
    try {
      
      conn = getConnection();
      //conn.setAutoCommit(false);
       
      linkID = storeObjectExternalRefLink(conn, ensemblInternalID, ensemblType, externalRefInternalID);
      
      //conn.commit();
    } catch (Exception e) {
      //rollback( conn );
      throw new AdaptorException("Failed to store link: ensemblInternalID="
      + ensemblInternalID + ",  ensemblType=" + ", "+ensemblType+",  externalRefInternalID="+externalRefInternalID, e);
    }finally {
      close( conn );
    }
    
    return linkID;
  }
  
  private long storeObjectExternalRefLink(Connection conn, long ensemblID, int ensemblType, long externalRefInternalID) throws SQLException, AdaptorException {
    
    String sql = "INSERT INTO object_xref ("
    + " ensembl_id, "  // 1
    + " ensembl_object_type, " // 2
    + " xref_id " // 3
    + " ) "
    + " VALUES (?, ?, ?) ";
    
    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setLong( 1, ensemblID);
    ps.setString( 2, ensemblTypeToString(ensemblType) );
    ps.setLong( 3, externalRefInternalID );
    
    return executeAutoInsert( ps, sql );
    
  }


  private String ensemblTypeToString(int ensemblType) throws AdaptorException {
    
    String str = null;
    switch (ensemblType) {
         case ExternalRef.GENE:
           str        = "Gene";
           break;
         case ExternalRef.TRANSLATION:
           str        = "Translation";
           break;
         case ExternalRef.TRANSCRIPT:
           str        = "Transcript";
           break;
         case ExternalRef.CLONE_FRAGMENT:
           str        = "RawContig";
           break;
         default:
           throw new AdaptorException("Type: " + ensemblType + " not a valid type of object, must " +
           " be one of ExternalRef.GENE, ExternalRef.TRANSLATION, ExternalRef.TRANSCRIPT or ExternalRef.CLONE_FRAGMENT");
       }
       
    return str;
  }


  public long store(ExternalRef externalRef) throws AdaptorException {
    
    long internalID = externalRef.getInternalID();
    
    Connection conn = null;
    try {
      
      conn = getConnection();
      //conn.setAutoCommit(false);
      
      internalID = store(conn, externalRef);
      
      //conn.commit();
    } catch (Exception e) {
      //rollback( conn );
      throw new AdaptorException("Failed to store externalRef: "
      + externalRef, e);
    }finally {
      close( conn );
    }
    
    return internalID;
  }
  
  
  
  long store(Connection conn, ExternalRef externalRef )
  throws SQLException, AdaptorException  {
    
    long internalID = storeInXRef( conn, externalRef);
    storeInExternalSynonym( conn, externalRef);
    externalRef.setDriver( driver );
    
    return internalID;
  }
  
  
  /**
   * @return newly assigned externalID.internalID after storing in database.
   */
  private long storeInXRef( Connection conn,
  ExternalRef externalRef)
  throws SQLException, AdaptorException  {
    
    // no need to supply xref_id value because it will be auto generated by db.
    String sql = "INSERT INTO xref ("
    + " dbprimary_acc, "  // 1
    + " external_db_id, " // 2
    + " display_label, " // 3
    + " version, " // 4
    + " description " // 5
    + " ) "
    + " VALUES (?, ?, ?, ?, ?) ";
    
    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setString( 1, externalRef.getPrimaryID() );
    ps.setLong( 2, externalRef.getExternalDbId() );
    ps.setString( 3, externalRef.getDisplayID() );
    ps.setString( 4, externalRef.getVersion() );
    ps.setString( 5, externalRef.getDescription() );
    
    long internalID = executeAutoInsert( ps, sql );
    externalRef.setInternalID( internalID );
    
    return internalID;
  }
  
  
  
  
  
  private void storeInExternalSynonym( Connection conn,
  ExternalRef externalRef)
  throws AdaptorException, SQLException {
    
    List synonyms = externalRef.getSynonyms();
    if ( synonyms==null || synonyms.size()==0 )
      return;
    
    String sql = "INSERT INTO external_synonym ("
    + " xref_id "  // 1
    + " ,synonym " // 2
    + " ) "
    + " VALUES (?, ?)";
    
    PreparedStatement ps = conn.prepareStatement(sql);
    final long internalID = externalRef.getInternalID();
    for(int i=0; i<synonyms.size(); ++i) {
      
      ps.setLong( 1, internalID );
      ps.setString( 2, (String)synonyms.get(i) );
      executeUpdate( ps, sql );
    }
    
  }
  
  
  /**
   * Deletes ExternalRef, with specified internalID, from datasource, does
   * nothing if externalRef is not in datasource.
   * @param internalID internalID of externalRef to be deleted
   */
  public void delete( long internalID ) throws AdaptorException {
    
    if ( internalID<1 ) return;
    
    Connection conn = null;
    try {
      
      conn = getConnection();
      conn.setAutoCommit(false);
      
      delete( conn, internalID );
      
      conn.commit();
    } catch (SQLException e) {
      rollback( conn );
      throw new AdaptorException("Failed to delete externalRef: "
      + internalID, e);
    }finally {
      close( conn );
    }
    
  }
  
  /**
   * Deletes externalRef from datasource, does nothing if externalRef is not
   * in datasource.
   * @param externalRef externalRef to be deleted
   */
  public void delete( ExternalRef externalRef ) throws AdaptorException {
    if ( externalRef==null ) return;
    delete( externalRef.getInternalID() );
    externalRef.setInternalID(0);
  }
  
  
  void delete(Connection conn, long xRefID) throws AdaptorException {
    
    executeUpdate(conn, "delete from xref where xref_id=" + xRefID);
    executeUpdate(conn, "delete from external_synonym where xref_id=" + xRefID);
    executeUpdate(conn, "delete from object_xref where xref_id=" + xRefID);
    
  }
}
