package org.ensembl.compara.driver.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ensembl.compara.datamodel.ComparaDataFactory;
import org.ensembl.compara.datamodel.impl.ComparaDataFactoryImpl;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.ConfigurationException;

/**
 * A set of convenieces for the drivers in the compara-db.
**/
public abstract class ComparaBaseAdaptor implements Adaptor {
  private static final Logger logger = Logger.getLogger(ComparaBaseAdaptor.class.getName());
  private ComparaDataFactory factory = null;
  private ComparaDriverImpl driver;

  private static HashMap columnNamesByTable = new HashMap();
  private List columnNames = new ArrayList();
  private String primeKey;
  
  private PreparedStatement selectAllStatement;
  private PreparedStatement selectStatement;
  private PreparedStatement insertStatement;
  private PreparedStatement updateStatement;
  private PreparedStatement deleteStatement;
  
  private String selectAllStatementString;
  private String selectStatementString;
  private String insertStatementString;
  private String updateStatementString;
  private String deleteStatementString;
  
  public ComparaBaseAdaptor(ComparaDriverImpl driver) {
    this.driver = driver;
  }

  public ComparaDataFactory getFactory(){
    if(factory == null){
      factory = new ComparaDataFactoryImpl();
    }//end if
    
    return factory;
  }//end getFactory
  
  public ComparaDriverImpl getDriver(){
    return driver;
  }//end getDriver


  /**
   * Does nothing.
   * @throws AdaptorException
   */
  public void closeAllConnections() throws AdaptorException {
  }

  /**
   * Does nothing.
   * @throws AdaptorException
   */
  public void clearCache() throws AdaptorException {
    
  }

  /**
   * Interrogate the base table and create the basic prepared statements.
  **/
  protected void initialise() throws AdaptorException{
    debug("initialising adaptor");
    Connection connection = null;
    try{
      connection = getConnection();

      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet resultSet = 
        metaData.getColumns(
          getDriver().getConfiguration().getProperty("database"), 
          null,//schema pattern
          getTableName(),
          null //table name pattern
        );

      while(resultSet.next()){
        getColumnNames().add(getTableName()+"."+resultSet.getString(4));
      }//end while

      getColumnNamesByTable().put(getTableName(), getColumnNames());
      
      resultSet = 
        metaData.getPrimaryKeys(
            getDriver().getConfiguration().getProperty("database"), 
            null,//schema pattern
            getTableName()
        );

      if(resultSet.next()){
        primeKey = resultSet.getString(4);
      }//end if

      if(resultSet.next()){
        throw new AdaptorException("Table : "+getTableName()+" has more than one primary key column: I can't handle this");
      }//end if
      
      if(primeKey == null){
        debug("Table :"+getTableName()+" has no declared prime key - I can't create the standard statement strings without one");
      }else{
        selectAllStatementString = createSelectAllStatementString();
        selectStatementString = createSelectStatementString(getPrimeKey());
        insertStatementString = createInsertStatementString();
        updateStatementString = createUpdateStatementString();
        deleteStatementString = createDeleteStatementString(getPrimeKey());
      }//end if
      
    }catch(SQLException exception){
      throw new AdaptorException(
        "Could not initialise "+this.getClass().getName()+":"+exception.getMessage(),
        exception
      );
    }finally{
      close(connection);
    }//end try
    debug("finished initialising adaptor");
  }//end initialise

  /**
   * Compara data adaptors pass connections back/forth through the pool like
   * everything else.
  **/
  protected Connection getConnection() throws AdaptorException{
    Connection connection = getDriver().getConnection();
    debug("fetching connection: "+connection.hashCode());
    return connection;
  }//end getConnection

  protected List initialiseTableColumns(String tableName) throws AdaptorException{
    Connection connection = null;
    debug("initialising table columns for: "+tableName);
    List columnNames = new ArrayList();
    try{
      connection = getConnection();

      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet resultSet = 
        metaData.getColumns(
          getDriver().getConfiguration().getProperty("database"), 
          null,//schema pattern
          tableName,
          null //table name pattern
        );

      while(resultSet.next()){
        columnNames.add(tableName+"."+resultSet.getString(4));
      }//end while
      
    }catch(SQLException exception){
      throw new AdaptorException(
        "Could not initialise "+this.getClass().getName()+":"+exception.getMessage(),
        exception
      );
    } finally {
      close(connection);
    }//end try

    getColumnNamesByTable().put(tableName, columnNames);
    
    return columnNames;
  }//end initialiseTableColumns
  
  public void setDriver(ComparaDriverImpl newValue){
    driver = newValue;
  }//end setDriver

  /**
   * By default this initialises the adaptor so it knows about its table, and
   * has basic knowlege of selects etc.
  **/
  protected void configure() throws ConfigurationException, AdaptorException {
    initialise();
  }//end configure

  /**
   * Run a select on the input table with the given input logical keys
   * to see if a record exists: if so, throw an AdaptorException.
  **/
  public void checkForDuplicateRow(
    String tableName,
    String primeKeyName, 
    Map logicalKeyPairs
  ) throws AdaptorException {
    ResultSet resultSet;
    Connection connection = null;
    String sql;
    StringBuffer sqlBuffer = 
      (new StringBuffer())
        .append("select ")
        .append(primeKeyName)
        .append(" from ")
        .append(tableName)
        .append(" where ");
    
    Iterator keyNames = logicalKeyPairs.keySet().iterator();
    String keyName = null;
    while(keyNames.hasNext()){
      keyName = (String)keyNames.next();
      sqlBuffer
        .append(keyName)
        .append(" = ")
        .append(logicalKeyPairs.get(keyName));
      
      if(keyNames.hasNext()){
        sqlBuffer.append(" and ");
      }//end if
    }//end while
    
    sql = sqlBuffer.append(";").toString();
    
    try{
      connection = getConnection();
      resultSet = connection.createStatement().executeQuery(sql);
      if(resultSet.next()){
        throw new AdaptorException("Duplicate row exists with Id: "+resultSet.getInt(1));
      }//end if
    }catch(SQLException exception){
      throw new AdaptorException("Problem issuing SQL "+sql, exception);
    }finally{
      close(connection);
    }//end try
  }//end checkForDuplicateRow
  
  /**
   * Convenience method for closing a Connection and printing a warning
   * message if an error occurs.
   */
  public void close( Connection connection ) {
    
    try { 
      if (connection!=null) connection.close(); 
      debug("closing connection: "+connection.hashCode());
    }catch (SQLException exception) { 
      getLogger().warning("Problem closing connection. "+exception.getMessage()); 
    }//end try
    
  }//end close

  protected String createDeleteStatementString(String columnName) 
  {
    StringBuffer statement = 
      new StringBuffer()
        .append("delete from ")
        .append(getTableName())
        .append(" where ")
        .append(columnName)
        .append(" =  ? ");

    debug("delete statement "+statement.toString());
    return statement.toString();
  }//end createDeleteStatement
  
  protected PreparedStatement createDeleteStatement(Connection connection) 
  throws AdaptorException
  {
    debug("delete statement "+getDeleteStatementString());
    return prepareStatement(connection, getDeleteStatementString());
  }//end createDeleteStatement

  protected String createSelectAllStatementString() 
  throws SQLException
  {
    StringBuffer statement = new StringBuffer().append("select ");
    statement.append(getColumnNames().get(0));
    for(int i=1;i<getColumnNames().size();i++){
      statement.append(",").append(getColumnNames().get(i));
    }//end for
    
    statement
      .append(" from ")
      .append(getTableName());
    
    debug("Select All statement "+statement.toString());
    return statement.toString();
  }//end createSelectAllStatementString
  
  protected PreparedStatement createSelectAllStatement(Connection connection) 
  throws AdaptorException
  {
    debug("Select All statement "+getSelectAllStatementString());
    return prepareStatement(connection, getSelectAllStatementString());
  }//end createSelectAllStatement

  /**
   * This creates a basic equijoin betweeen the input tables, based on
   * the pairs of columns passed in. THose columns should be fully qualified!!
  **/
  protected StringBuffer createJoinedSelect(
    String[] tableNames,
    String[] pairedJoinColumns
  )throws AdaptorException{
    
    StringBuffer select = new StringBuffer();
    List columnNames = null;
    
    if(tableNames.length <= 0){
      throw new AdaptorException("No tables passed into join-select creation");
    }//end if
      
    
    if(2*(tableNames.length-1) != pairedJoinColumns.length){
      throw new AdaptorException("Number of join columns passed into join-select creation does not match number of tables");
    }//end if
    
    //
    //Create the list of table columns to join
    select.append("select ");
    for(int tableCount =0; tableCount<tableNames.length; tableCount++){
      
      columnNames = getColumnNames(tableNames[tableCount]);
      for(int columnCount=0; columnCount<columnNames.size(); columnCount++){
        if(tableCount==0 && columnCount==0){
          select.append(columnNames.get(columnCount));
        }else{
          select.append(",").append(columnNames.get(columnCount));
        }//end if
      }//end for

    }//end for

    //
    //add the list of joined tables.
    select.append(" from ");
    for(int tableCount =0; tableCount<tableNames.length; tableCount++){
      if(tableCount ==0){
        select.append(tableNames[tableCount]);
      }else{
        select.append(",").append(tableNames[tableCount]);
      }//end if
    }//end for
    
    //
    //add the join conditions implied by the columns passed in.
    select.append(" where ");
    for(
      int joinColumnCount =0; 
      joinColumnCount<pairedJoinColumns.length; 
      joinColumnCount = joinColumnCount+2 //jump the list in pairs
    ){
      if(joinColumnCount == 0){
        select
          .append(pairedJoinColumns[joinColumnCount])
          .append(" = ")
          .append(pairedJoinColumns[joinColumnCount+1]);
      }else{
        select
          .append(" and ")
          .append(pairedJoinColumns[joinColumnCount])
          .append(" = ")
          .append(pairedJoinColumns[joinColumnCount+1]);
      }//end if
    }//end for
    
    return select;
  }//end createJoinedSelect
  
  protected StringBuffer createSelectUpToWhere(){
    StringBuffer statement = new StringBuffer().append("select ");
    statement.append(getColumnNames().get(0));
    for(int i=1;i<getColumnNames().size();i++){
      statement.append(",").append(getColumnNames().get(i));
    }//end for
    
    statement
      .append(" from ")
      .append(getTableName());
      
    return statement;
  }//end createSelectUpToWhere
  
  protected StringBuffer addEqualsClause(String field, StringBuffer predicate){
    if(predicate.length() > 0){
      predicate
        .append(" AND ")
        .append(field)
        .append(" = ")
        .append(" ? ");
    }else{
      predicate
        .append(" where ")
        .append(field)
        .append(" = ")
        .append(" ? ");
    }//end if
    
    return predicate;
  }//end addEqualsClause
  
  
  protected StringBuffer addGEClause(String field, StringBuffer predicate){
    if(predicate.length() > 0){
      predicate
        .append(" AND ")
        .append(field)
        .append(" >= ")
        .append(" ? ");
    }else{
      predicate
        .append(" where ")
        .append(field)
        .append(" >= ")
        .append(" ? ");
    }//end if
    
    return predicate;
  }//end addEqualsClause
  
  protected StringBuffer addLEClause(String field, StringBuffer predicate){
    if(predicate.length() > 0){
      predicate
        .append(" AND ")
        .append(field)
        .append(" <= ")
        .append(" ? ");
    }else{
      predicate
        .append(" where ")
        .append(field)
        .append(" <= ")
        .append(" ? ");
    }//end if
    
    return predicate;
  }//end addEqualsClause
  
  private String createSelectStatementString(String columnName) 
  {
    StringBuffer statement = new StringBuffer().append("select ");
    statement.append(getColumnNames().get(0));
    
    for(int i=1;i<getColumnNames().size();i++){
      statement.append(",").append(getColumnNames().get(i));
    }//end for
    
    statement
      .append(" from ")
      .append(getTableName())
      .append(" where ")
      .append(columnName)
      .append(" = ")
      .append(" ? ");
    
    debug("Select statement "+statement.toString());
    return statement.toString();
  }//end createSelectStatementString

  protected PreparedStatement createSelectStatement(Connection connection) 
  throws AdaptorException
  {
    debug("Creating Select statement "+getSelectStatementString());
    return prepareStatement(connection, getSelectStatementString());
  }//end createSelectStatement
  
  protected String createInsertStatementString() 
  {
    StringBuffer statement = 
      new StringBuffer()
        .append("insert into ")
        .append(getTableName())
        .append(" ( ");
    
    statement.append(getColumnNames().get(0));
    for(int i=1;i<getColumnNames().size();i++){
      statement.append(",").append(getColumnNames().get(i));
    }//end for
    
    statement
      .append(" )  values ( ");

    statement.append(" ? ");
    for(int i=1;i<getColumnNames().size();i++){
      statement.append(", ? ");
    }//end for

    statement
      .append(" ); ");

    debug("insert statement "+statement.toString());
    return statement.toString();
  }//end createInsertStatementString
  
  protected PreparedStatement createInsertStatement(Connection connection) 
  throws AdaptorException
  {
    debug("insert statement "+getInsertStatementString());
    return prepareStatement(connection, getInsertStatementString());
  }//end createInsertStatement
  
  protected String createUpdateStatementString() 
  {
    StringBuffer statement = 
      new StringBuffer()
        .append("update ")
        .append(getTableName())
        .append(" set ");

    statement
      .append(getColumnNames().get(0))
      .append(" = ")
      .append(" ? ");

    for(int i=1;i<getColumnNames().size();i++){
      statement
        .append(",")
        .append(getColumnNames().get(i))
        .append(" = ")
        .append(" ? ");
    }//end for
    
    statement
      .append(" ; ");

    debug("update statement "+statement.toString());
    return statement.toString();
  }//end createUpdateStatementString

  protected PreparedStatement createUpdateStatement(Connection connection) 
  throws AdaptorException
  {
    debug("update statement "+getUpdateStatementString());
    return prepareStatement(connection, getUpdateStatementString());
  }//end createUpdateStatement

  
  protected String  getSelectAllStatementString(){
    return selectAllStatementString;
  }//end getSelectStatement
  
  protected String getSelectStatementString(){
    return selectStatementString;
  }//end getSelectStatementString
  
  protected String getInsertStatementString(){
    return insertStatementString;
  }//end getInsertStatementString
  
  protected String getUpdateStatementString(){
    return updateStatementString;
  }//end getUpdateStatementString
  
  protected String getDeleteStatementString(){
    return deleteStatementString;
  }//end getDeleteStatementString

  protected List getColumnNames(){
    return columnNames;
  }//end getColumnNames
  
  protected List getColumnNames(String table) throws AdaptorException{
    List names =  (List)getColumnNamesByTable().get(table);
    if(names == null){
      names = initialiseTableColumns(table);
    }//end if
    
    return names;
  }//end getColumnNames
  
  protected HashMap getColumnNamesByTable(){
    return columnNamesByTable;
  }//end getColumnNamesByTable
  
  protected String getPrimeKey(){
    return primeKey;
  }//end getPrimeKey
  

  /**
   * Connection used to prepare the statement is pulled from the pool.
   * This logs the statement as it gets prepared - just a nice way of making
   * sure we see logged statements.
  **/
  protected PreparedStatement prepareStatement(Connection connection, String statementText) throws AdaptorException{
    PreparedStatement statement = null;
    try{
      statement = connection.prepareStatement(statementText.toString());
      debug("Created prepared statement: "+statementText);
    }catch(SQLException exception){
      throw new AdaptorException("Cannot prepare statement: "+statementText, exception);
    }//end try
    
    return statement;
  }//end prepareStatement
    
  
  /**
   * Create a new select statement with the where clause passed in
  **/
  protected PreparedStatement prepareSelectWithWhereClause(Connection connection, String statementKey) 
  throws AdaptorException{
    PreparedStatement statement = null;
    StringBuffer statementText = null;
    statementText = createSelectUpToWhere().append(statementKey);
    statement = prepareStatement(connection, statementText.toString());
    return statement;
  }//end prepareOrFetchStatement
  
  /**
   * Returns name of the key table this adaptor is attached to:
   * inserts, fetches and such-like are driven off this table name.
  **/
  protected abstract String getTableName();

  protected static Logger getLogger(){
    return logger;
  }//end getLogger
  
  protected void debug(String message){
    getLogger().fine(message);
  }//end log
  
  protected void warn(String message){
    getLogger().warning(message);
  }//end warn
  
  public PersistentImpl fetch(Object id) throws AdaptorException{
    Connection connection = getConnection();
    PreparedStatement statement = createSelectStatement(connection);
    PersistentImpl returnObject = null;
    ResultSet set;
    ArrayList arguments = new ArrayList();
    arguments.add(id);

    set = executeStatement(statement, arguments);

    try{
      if(set.next()){
        returnObject = convertResultSetToObject(set);
      }//end if

      if(set.next()){
        throw new AdaptorException("fetch by id "+id+" fetched more than one exception");
      }//end if
    }catch(SQLException exception){
      throw new AdaptorException("problems fetching by id "+id, exception);
    }finally{
      close(connection);
    }

    return returnObject;
  }//end fetch

  /**
   * Input - prepared statement and list of arguments
   * Output - a Resultset
  **/
  protected ResultSet executeStatement(
    PreparedStatement statement, 
    List arguments
  )throws AdaptorException{
    
    ResultSet set;
    StringBuffer argString = new StringBuffer();
    int rowCount = 0;

    try{
      for(int i=0; i < arguments.size(); i++){
        argString.append(arguments.get(i)).append(":");
        statement.setObject(i+1, arguments.get(i));
      }//end for
      
      debug("Executing prepared statement with arguments: "+argString.toString());
    
      set  = statement.executeQuery();

      return set;
      
    }catch(SQLException exception){
      throw new AdaptorException("Error executing SQL: "+exception.getMessage(), exception);
    }
  }//end executeStatement
  
  protected List executeStatementAndConvertResultToPersistent(
    PreparedStatement statement, 
    List arguments
  )throws AdaptorException{
    
    List returnedList = new ArrayList();
    Persistent object = null;
    ResultSet set;
    StringBuffer argString = new StringBuffer();
    int rowCount = 0;
    
    try{
      
      set = executeStatement(statement, arguments);

      while(set.next()){
        rowCount++;
        returnedList.add(convertResultSetToObject(set));
      }//end if
      
      debug("Rows returned: "+rowCount);
      
    }catch(SQLException exception){
      throw new AdaptorException("Error executing SQL: "+exception.getMessage(), exception);
    }

    return returnedList;
  }//end executeStatementAndConvertResultToPersistent
 
  /**
   * fetches all records.
  **/
  public List fetch() throws AdaptorException{
    PreparedStatement statement = null;
    List returnedList = new ArrayList();
    PersistentImpl object = null;
    ResultSet set;
    Connection connection = null;
    
    try{
      connection = getConnection();
      statement = createSelectAllStatement(connection);
      set  = statement.executeQuery();
    
      while(set.next()){
        object = convertResultSetToObject(set);
        returnedList.add(object);
      }//end if
      
    }catch(SQLException exception){
      throw new AdaptorException("Error executing SQL: "+exception.getMessage(), exception);
    }finally{
      close(connection);
    }//end try

    return returnedList;
  }//end fetch
  
  public void store(Persistent persistentObject) throws AdaptorException{
    Connection connection = getConnection();
    PreparedStatement statement = createInsertStatement(connection);
    ResultSet set;
    HashMap columnValues = mapObjectToColumns(persistentObject);
    
    //do validation of input object
    validate(persistentObject);
    
    //
    //Make sure the record doesn't already exist
    checkForDuplicateRow(getTableName(), getPrimeKey(), getLogicalKeyPairs(persistentObject));
    
    try{
      
      for(int counter=0; counter < getColumnNames().size(); counter++){
        statement.setObject(
          counter+1,
          columnValues.get((String)getColumnNames().get(counter))
        );
      }//end for

      statement.execute();

    }catch(SQLException exception){
      throw new AdaptorException("Error executing SQL: "+exception.getMessage(), exception);
    }finally{
      close(connection);
    }//end try
  }//end store
  
  public void delete(Object id){
  }//end delete
  
  protected PersistentImpl convertResultSetToObject(ResultSet set) 
  throws SQLException, AdaptorException{
    PersistentImpl returnObject = createNewObject();
    HashMap columnValues = new HashMap();
    
    for(int counter = 0; counter< getColumnNames().size(); counter++){
      columnValues.put(
        (String)getColumnNames().get(counter), 
        set.getObject((String)getColumnNames().get(counter))
      );
    }//end for
    
    mapColumnsToObject(columnValues, returnObject);
    returnObject.setDriver(getDriver());
    return returnObject;
  }//end convertResultSetToObject  
  
  protected abstract HashMap mapObjectToColumns(Persistent object);
  
  protected abstract void mapColumnsToObject(HashMap columns, Persistent object);
  
  protected abstract PersistentImpl createNewObject();
  
  protected abstract void validate(Persistent object) throws AdaptorException;
  
  public abstract HashMap getLogicalKeyPairs(Persistent genomeDB) throws AdaptorException;
  
}
