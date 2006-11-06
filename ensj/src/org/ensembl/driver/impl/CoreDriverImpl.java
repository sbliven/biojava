/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,locatio but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.driver.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.OligoArrayAdaptor;
import org.ensembl.driver.OligoFeatureAdaptor;
import org.ensembl.driver.OligoProbeAdaptor;
import org.ensembl.driver.AnalysisAdaptor;
import org.ensembl.driver.AssemblyExceptionAdaptor;
import org.ensembl.driver.AssemblyMapperAdaptor;
import org.ensembl.driver.ConfigurationException;
import org.ensembl.driver.CoordinateSystemAdaptor;
import org.ensembl.driver.DnaDnaAlignmentAdaptor;
import org.ensembl.driver.DnaProteinAlignmentAdaptor;
import org.ensembl.driver.ExonAdaptor;
import org.ensembl.driver.ExternalDatabaseAdaptor;
import org.ensembl.driver.ExternalRefAdaptor;
import org.ensembl.driver.GeneAdaptor;
import org.ensembl.driver.KaryotypeBandAdaptor;
import org.ensembl.driver.LocationConverter;
import org.ensembl.driver.LoggingManager;
import org.ensembl.driver.MarkerAdaptor;
import org.ensembl.driver.MarkerFeatureAdaptor;
import org.ensembl.driver.MiscFeatureAdaptor;
import org.ensembl.driver.MiscSetAdaptor;
import org.ensembl.driver.PredictionExonAdaptor;
import org.ensembl.driver.PredictionTranscriptAdaptor;
import org.ensembl.driver.ProteinFeatureAdaptor;
import org.ensembl.driver.QtlAdaptor;
import org.ensembl.driver.QtlFeatureAdaptor;
import org.ensembl.driver.RepeatConsensusAdaptor;
import org.ensembl.driver.RepeatFeatureAdaptor;
import org.ensembl.driver.SequenceAdaptor;
import org.ensembl.driver.SequenceRegionAdaptor;
import org.ensembl.driver.SimpleFeatureAdaptor;
import org.ensembl.driver.StableIDEventAdaptor;
import org.ensembl.driver.SupportingFeatureAdaptor;
import org.ensembl.driver.TranscriptAdaptor;
import org.ensembl.driver.TranslationAdaptor;
import org.ensembl.util.PropertiesUtil;
import org.ensembl.variation.driver.VariationDriver;

/**
 * CoreDriverImpl provides read and write access to ensembl-core databases stored
 * on a MySQL server.
 * 
 * <p>
 * There are several ways to create a CoreDriverImpl instance:
 * 
 * <ul>
 * <li>Directly using one of the constructors. e.g. <code>
 * <pre>
 * CoreDriver driver = new CoreDriverImpl(&quot;ensembldb.ensembl.org&quot;,
 *     &quot;homo_sapiens_core_24_34e&quot;, &quot;anonymous&quot;);CoreDriver latestDriver = new CoreDriverImpl(&quot;ensembldb.ensembl.org&quot;, 
 *                                          &quot;homo_sapiens_core&quot;, 
 *                                          &quot;anonymous&quot;,
 *                                          true);
 *    
 *   
 *  
 * </pre>
 * </code>
 * 
 * </li>
 * 
 * <li>Indirectly via the DriverManager using a properties object. The
 * DriverManager can create a driver from a property object. e.g. <code>
 * <pre>
 * Properties p = new Properties();
 * p.put(&quot;driver&quot;, &quot;org.ensembl.driver.impl.CoreDriverImpl&quot;);
 * p.put(&quot;host&quot;, &quot;ensembldb.ensembl.org&quot;);
 * p.put(&quot;database&quot;, &quot;SOME_core_DATABASE&quot;);
 * p.put(&quot;user&quot;, &quot;USER_NAME&quot;);
 * CoreDriver driver = DriverManager.load(&quot;driver.properties&quot;);
 * </pre>
 * </code></li>
 * 
 * <li>Indirectly via the DriverManager using a configuration properties file.
 * If we have a properties file <i>driver.properties </i> containing these lines
 * ...
 * 
 * <pre>
 * 
 *  
 *   
 *     driver=org.ensembl.driver.impl.CoreDriverImpl 
 *     host=ensembldb.ensembl.org 
 *     database=SOME_core_DATABASE 
 *     user=USER_NAME
 *    
 *   
 *  
 * </pre>
 * 
 * 
 * ... then we could instantiate a CoreDriverImpl like this <code>
 * <pre>
 * CoreDriver driver = DriverManager.load(&quot;driver.properties&quot;);
 * </pre>
 * </code>
 * 
 * The advantage of this approach over hard coding the constructor parameters or
 * properties into the code is flexibility. It makes it possible to change the
 * datasource an application uses without changing and recompiling the code.
 * </li>
 * 
 * </ul>
 * </p>
 * 
 * 
 * <p>
 * These are the necessary and optional parameters for configuring a CoreDriverImpl
 * instance:
 * 
 * <ul>
 * <b>Necessary configuration parameters with example values: </b>
 * <li>host = ensembldb.ensembl.org
 * <li>either (a)database = SOME_core_DATABASE or (b)database_prefix =
 * SOME_DATABASE_PREFIX
 * <li>user = USER_NAME
 * </ul>
 * 
 * <ul>
 * <b>Optional (advanced) configuration parameters with default values: </b>
 * 
 * <li>port = 3306
 * <li>password = ""
 * <li>jdbc_driver = org.gjt.mm.mysql.CoreDriver
 * <li>connection_pool_size = 10 (min is 2)
 * <li>path = SOME_core_DATABASE (defaults to "database" value defined above.
 * This value can be used to retrieve the driver via the DriverManager. e.g.
 * DriverManager.get("SOME_core_DATABASE"))
 * <li>ensembl_driver = org.ensembl.driver.impl.CoreDriverImpl
 * (specifying a different value here would cause a different driver class to be
 * loaded.)
 * 
 * 
 * 
 * <li>It is possible to specify alternative database configurations per
 * adaptor. This means that each adaptor CAN connect to a separate database.
 * Adaptor specific parameters begin with the adaptors type (
 * <code>someAdaptor.getType()</code> )followed by a "." then the connection
 * parameter.
 * 
 * <ul>
 * <li>For example, if you wish to retrieve sequence from the SEQUENCE_DATABASE
 * on the same host as the rest of the adaptors you would add this property:
 * sequence.database = SEQUENCE_DATABASE
 * 
 * <li>If it is on a different host then add these parameters: sequence.host =
 * SEQUENCE_HOST, sequence.user = SEQUENCE_USER
 * 
 * </ul>
 * 
 * 
 * <li>Variations are stored in a separate SNP database accessed using these
 * parameters:
 * <ul>
 * <li>variation.host = ensembldb.ensembl.org (default value is _host_ )
 * <li>variation.port = 3306 (default value is _port_ )
 * <li>variation.database = SOME_variation_DATABASE ( default value is derived
 * from "database" by replacing "core" with "variation")
 * <li>variation.user = USER_NAME (defaults to _user_)
 * <li>variation.password = (defaults to "password")
 * </ul>
 * 
 * 
 * 
 * <p>
 * Once the driver is available it is possible to get the adaptors required to
 * access the different data types. For example, if we wanted to load all the
 * genes from the minus strand of Chromosome 6 between 1m bases to 2m,we could
 * do this:
 * </p>
 * 
 * <ol>
 * <li>Get the gene adaptor from the driver. <br>
 * <code>GeneAdaptor geneAdaptor = driver.getGeneAdaptor();</code></br>
 * <li>Get the genes from the adaptor. <br>
 * <code>List genes = geneAdaptor.fetch(new Location("chromosome:6:1m-2m:-1"));</code>
 * </br>
 * </ol>
 * 
 * <p>
 * Setting environment variable "ensj.debug" (e.g. java -Densj.debug ... )
 * causes the database connection string to be displayed when the driver first
 * attempts to connect to the database. This is useful when you want to check
 * which database is being connected to.
 * </p>
 * 
 * 
 * <p>
 * This class is thread safe.
 * </p>
 * 
 * @see <a href="../../../Example.java">Example.java source </a>
 */
public class CoreDriverImpl extends EnsemblDriverImpl implements
    org.ensembl.driver.CoreDriver, org.ensembl.driver.Driver {

  private static final Logger logger = Logger.getLogger(CoreDriverImpl.class
      .getName());

  private boolean databaseSet = false;

  private String defaultAssembly;
  
  /** Sister variation driver. */
  private VariationDriver vdriver = null;

  /**
   * Creates an unitialised driver with no adaptor.
   * 
   * Call initialise(Properties) to initialise this driver.
   * 
   * @see #initialise(Properties)
   */
  public CoreDriverImpl() {
    super();
  }

  /**
   * Constructs a driver using the specified configuration object.
   * 
   * The configuration is passed straight to initialise(Object).
   * 
   * @param configuration
   *          configuration parameters.
   * @throws AdaptorException
   * @see EnsemblDriverImpl#initialise(Properties)
   */
  public CoreDriverImpl(Properties configuration) throws AdaptorException {
    super(configuration, true);
  }

  /**
   * Constructs a driver pointing at the specified database. Assumes no password
   * and port = 3306.
   * 
   * @param host
   *          computer hosting mysqld database
   * @param database
   *          database name
   * @param user
   *          user name
   */
  public CoreDriverImpl(String host, String database, String user)
      throws AdaptorException {
    super(host, database, user, null, null, false);
  }

  /**
   * Constructs a driver pointing at the specified database. Assumes port =
   * 3306.
   * 
   * @param host
   *          computer hosting mysqld database
   * @param database
   *          database name
   * @param user
   *          user name
   * @param password
   *          password
   */
  public CoreDriverImpl(String host, String database, String user, String password)
      throws AdaptorException {
    super(host, database, user, null, null, false);
  }

  /**
   * Constructs a driver pointing at the specified database.
   * 
   * @param host
   *          computer hosting mysqld database
   * @param database
   *          database name
   * @param user
   *          user name
   * @param password
   *          password
   * @param port
   *          port on host computer that mysqld is running on
   */
  public CoreDriverImpl(String host, String database, String user,
      String password, String port) throws AdaptorException {
    super(host, database, user, password, port, false);
  }

  /**
   * Constructs a driver pointing at the specified database/database-prefix.
   * Assumes no password and port = 3306.
   * 
   * @param host
   *          computer hosting mysqld database
   * @param database
   *          database name
   * @param user
   *          user name
   * @param databaseIsPrefix
   *          true is database is to be used as a prefix or false if it is to be
   *          used unmodified as a database name.
   */
  public CoreDriverImpl(String host, String database, String user,
      boolean databaseIsPrefix) throws AdaptorException {
    super(host, database, user, null, null, databaseIsPrefix);
  }

  /**
   * Constructs a driver pointing at the specified database/database-prefix.
   * Assumes port = 3306.
   * 
   * @param host
   *          computer hosting mysqld database
   * @param database
   *          database name
   * @param user
   *          user name
   * @param password
   *          password
   * @param databaseIsPrefix
   *          true is database is to be used as a prefix or false if it is to be
   *          used unmodified as a database name.
   */
  public CoreDriverImpl(String host, String database, String user,
      String password, boolean databaseIsPrefix) throws AdaptorException {
    super(host, database, user, null, null, databaseIsPrefix);
  }

  /**
   * Constructs a driver pointing at the specified database/database-prefix.
   * 
   * @param host
   *          computer hosting mysqld database
   * @param database
   *          database name
   * @param user
   *          user name
   * @param password
   *          password
   * @param port
   *          port on host computer that mysqld is running on
   * @param databaseIsPrefix
   *          true is database is to be used as a prefix or false if it is to be
   *          used unmodified as a database name.
   */
  public CoreDriverImpl(String host, String database, String user,
      String password, String port, boolean databaseIsPrefix)
      throws AdaptorException {
    super(host, database, user, password, port, databaseIsPrefix);
  }

  /**
   * Call back method called by initialise(Object) that loads the adaptors.
   */
  protected void loadAdaptors() throws AdaptorException, ConfigurationException {

    super.loadAdaptors();
    


    // can't load type adaptors if no database set. Check various ways database
    // could be set.
//    boolean databaseSpecified = false;
//    String url = configuration.getProperty("connection_url");
//    if (url != null)
//      databaseSpecified = Pattern.matches(".+//.+/.+", url);
//    else
//      databaseSpecified = configuration.getProperty("connection_string") != null
//          && (!configuration.getProperty("database").equals("") || configuration.getProperty("database_prefix") != null);
//    if (!databaseSpecified)
//      return;


    addAdaptor(new CoordinateSystemAdaptorImpl(this));
    addAdaptor(new LocationConverterImpl(this));
    addAdaptor(new SequenceAdaptorImpl(this));
    addAdaptor(new AnalysisAdaptorImpl(this));
    addAdaptor(new ExonAdaptorImpl(this));
    addAdaptor(new GeneAdaptorImpl(this));

    addAdaptor(new TranscriptAdaptorImpl(this));
    addAdaptor(new TranslationAdaptorImpl(this));

    addAdaptor(new ExternalRefAdaptorImpl(this));
    addAdaptor(new StableIDEventAdaptorImpl(this));
    addAdaptor(new ExternalDatabaseAdaptorImpl(this));
    addAdaptor(new SupportingFeatureAdaptorImpl(this));
    addAdaptor(new RepeatFeatureAdaptorImpl(this));

    addAdaptor(new MarkerAdaptorImpl(this));
    addAdaptor(new RepeatConsensusAdaptorImpl(this));

    addAdaptor(new DnaProteinAlignmentAdaptorImpl(this));
    addAdaptor(new DnaDnaAlignmentAdaptorImpl(this));
    addAdaptor(new SimpleFeatureAdaptorImpl(this));
    addAdaptor(new ProteinFeatureAdaptorImpl(this));
    addAdaptor(new PredictionTranscriptAdaptorImpl(this));

    addAdaptor(new PredictionExonAdaptorImpl(this));

    addAdaptor(new AssemblyMapperAdaptorImpl(this));
    addAdaptor(new AssemblyExceptionAdaptorImpl(this));

    addAdaptor(new SequenceRegionAdaptorImpl(this));

    addAdaptor(new KaryotypeBandAdaptorImpl(this));

    addAdaptor(new MiscFeatureAdaptorImpl(this));
    addAdaptor(new MiscSetAdaptorImpl(this));

    addAdaptor(new MarkerFeatureAdaptorImpl(this));

    addAdaptor(new QtlAdaptorImpl(this));

    addAdaptor(new QtlFeatureAdaptorImpl(this));

    addAdaptor(new OligoFeatureAdaptorImpl(this));

    addAdaptor(new OligoProbeAdaptorImpl(this));

    addAdaptor(new OligoArrayAdaptorImpl(this));

  }

  /**
   * Does nothing other than write config to logger.fine().
   * 
   * @param conf
   *          object to be modified if necessary.
   * @throws ConfigurationException
   */
  protected void processConfiguration(Configuration conf)
      throws ConfigurationException {

    super.processConfiguration(conf);
    if (logger.isLoggable(Level.FINE)) 
      logger.fine("Input conf : " + conf
      +"\nFinal conf : " + this.configuration);

  }

  /**
   * @return name of default assembly in database.
   */
  String getDefaultAssembly() throws AdaptorException {
    if (defaultAssembly == null) {
      Connection conn = null;
      String sql = "SELECT meta_value FROM meta where meta_key='assembly.default'";
      try {
        conn = getConnection();
        ResultSet rs = conn.createStatement().executeQuery(sql);
        rs.next();
        defaultAssembly = rs.getString(1);
      } catch (Exception e) {
        throw new AdaptorException(
            "Failed to get default assembly from database:" + sql, e);
      } finally {
        close(conn);
      }
    }

    return defaultAssembly;
  }

  /**
   * Test that driver starts with specified config files.
   */
  public static void main(String[] args) throws Exception {

    LoggingManager.configure();

    Properties userConfig = new Properties();

    for (int i = 0; i < args.length; ++i) {
      userConfig.putAll(PropertiesUtil.createProperties(args[i]));
    }

    System.out
        .println("USER SETTINGS:\n" + PropertiesUtil.toString(userConfig));
    System.out.flush();

    CoreDriverImpl d = new CoreDriverImpl(userConfig);

    System.out.println("FINAL SETTINGS:\n"
        + PropertiesUtil.toString(d.getConfiguration()));
  }

  public synchronized TranslationAdaptor getTranslationAdaptor()
      throws AdaptorException {
    return (TranslationAdaptor) getAdaptor(TranslationAdaptor.TYPE);
  }

  public synchronized ExternalRefAdaptor getExternalRefAdaptor()
      throws AdaptorException {
    return (ExternalRefAdaptor) getAdaptor(ExternalRefAdaptor.TYPE);
  }

  public synchronized SequenceAdaptor getSequenceAdaptor()
      throws AdaptorException {
    return (SequenceAdaptor) getAdaptor(SequenceAdaptor.TYPE);
  }

  public synchronized LocationConverter getLocationConverter()
      throws AdaptorException {
    return (LocationConverter) getAdaptor(LocationConverter.TYPE);
  }

  public synchronized TranscriptAdaptor getTranscriptAdaptor()
      throws AdaptorException {
    return (TranscriptAdaptor) getAdaptor(TranscriptAdaptor.TYPE);
  }

  public synchronized RepeatConsensusAdaptor getRepeatConsensusAdaptor()
      throws AdaptorException {
    return (RepeatConsensusAdaptor) getAdaptor(RepeatConsensusAdaptor.TYPE);
  }

  public synchronized ExternalDatabaseAdaptor getExternalDatabaseAdaptor()
      throws AdaptorException {
    return (ExternalDatabaseAdaptor) getAdaptor(ExternalDatabaseAdaptor.TYPE);
  }

  public synchronized ExonAdaptor getExonAdaptor() throws AdaptorException {
    return (ExonAdaptor) getAdaptor(ExonAdaptor.TYPE);
  }

  public synchronized GeneAdaptor getGeneAdaptor() throws AdaptorException {
    return (GeneAdaptor) getAdaptor(GeneAdaptor.TYPE);
  }

  public synchronized AnalysisAdaptor getAnalysisAdaptor()
      throws AdaptorException {
    return (AnalysisAdaptor) getAdaptor(AnalysisAdaptor.TYPE);
  }

  public synchronized RepeatFeatureAdaptor getRepeatFeatureAdaptor()
      throws AdaptorException {
    return (RepeatFeatureAdaptor) getAdaptor(RepeatFeatureAdaptor.TYPE);
  }

  public synchronized SupportingFeatureAdaptor getSupportingFeatureAdaptor()
      throws AdaptorException {
    return (SupportingFeatureAdaptor) getAdaptor(SupportingFeatureAdaptor.TYPE);
  }

  public synchronized MarkerAdaptor getMarkerAdaptor() throws AdaptorException {
    return (MarkerAdaptor) getAdaptor(MarkerAdaptor.TYPE);
  }

  public synchronized DnaProteinAlignmentAdaptor getDnaProteinAlignmentAdaptor()
      throws AdaptorException {
    return (DnaProteinAlignmentAdaptor) getAdaptor(DnaProteinAlignmentAdaptor.TYPE);
  }

  public synchronized DnaDnaAlignmentAdaptor getDnaDnaAlignmentAdaptor()
      throws AdaptorException {
    return (DnaDnaAlignmentAdaptor) getAdaptor(DnaDnaAlignmentAdaptor.TYPE);
  }

  public synchronized SimpleFeatureAdaptor getSimpleFeatureAdaptor()
      throws AdaptorException {
    return (SimpleFeatureAdaptor) getAdaptor(SimpleFeatureAdaptor.TYPE);
  }

  public synchronized StableIDEventAdaptor getStableIDEventAdaptor()
      throws AdaptorException {
    return (StableIDEventAdaptor) getAdaptor(StableIDEventAdaptor.TYPE);
  }

  public synchronized ProteinFeatureAdaptor getProteinFeatureAdaptor()
      throws AdaptorException {
    return (ProteinFeatureAdaptor) getAdaptor(ProteinFeatureAdaptor.TYPE);
  }

  public synchronized PredictionTranscriptAdaptor getPredictionTranscriptAdaptor()
      throws AdaptorException {
    return (PredictionTranscriptAdaptor) getAdaptor(PredictionTranscriptAdaptor.TYPE);
  }

  public synchronized PredictionExonAdaptor getPredictionExonAdaptor()
      throws AdaptorException {
    return (PredictionExonAdaptor) getAdaptor(PredictionExonAdaptor.TYPE);
  }

  public synchronized CoordinateSystemAdaptor getCoordinateSystemAdaptor()
      throws AdaptorException {

    return (CoordinateSystemAdaptor) getAdaptor(CoordinateSystemAdaptor.TYPE);

  }

  public synchronized AssemblyMapperAdaptor getAssemblyMapperAdaptor()
      throws AdaptorException {
    return (AssemblyMapperAdaptor) getAdaptor(AssemblyMapperAdaptor.TYPE);
  }

  public synchronized AssemblyExceptionAdaptor getAssemblyExceptionAdaptor()
      throws AdaptorException {
    return (AssemblyExceptionAdaptor) getAdaptor(AssemblyExceptionAdaptor.TYPE);
  }

  public synchronized SequenceRegionAdaptor getSequenceRegionAdaptor()
      throws AdaptorException {
    return (SequenceRegionAdaptor) getAdaptor(SequenceRegionAdaptor.TYPE);
  }

  public synchronized KaryotypeBandAdaptor getKaryotypeBandAdaptor()
      throws AdaptorException {
    return (KaryotypeBandAdaptor) getAdaptor(KaryotypeBandAdaptor.TYPE);
  }

  public synchronized MiscFeatureAdaptor getMiscFeatureAdaptor()
      throws AdaptorException {
    return (MiscFeatureAdaptor) getAdaptor(MiscFeatureAdaptor.TYPE);
  }

  public synchronized MiscSetAdaptor getMiscSetAdaptor()
      throws AdaptorException {
    return (MiscSetAdaptor) getAdaptor(MiscSetAdaptor.TYPE);
  }

  public synchronized MarkerFeatureAdaptor getMarkerFeatureAdaptor()
      throws AdaptorException {
    return (MarkerFeatureAdaptor) getAdaptor(MarkerFeatureAdaptor.TYPE);
  }

  public synchronized QtlAdaptor getQtlAdaptor() throws AdaptorException {
    return (QtlAdaptor) getAdaptor(QtlAdaptor.TYPE);
  }

  public synchronized QtlFeatureAdaptor getQtlFeatureAdaptor()
      throws AdaptorException {
    return (QtlFeatureAdaptor) getAdaptor(QtlFeatureAdaptor.TYPE);
  }

  public OligoProbeAdaptor getOligoProbeAdaptor() throws AdaptorException {
    return (OligoProbeAdaptor) getAdaptor(OligoProbeAdaptor.TYPE);
  }

  public OligoFeatureAdaptor getOligoFeatureAdaptor() throws AdaptorException {
    return (OligoFeatureAdaptor) getAdaptor(OligoFeatureAdaptor.TYPE);
  }

  public OligoArrayAdaptor getOligoArrayAdaptor() throws AdaptorException {
    return (OligoArrayAdaptor) getAdaptor(OligoArrayAdaptor.TYPE);
  }

  /* (non-Javadoc)
   * @see org.ensembl.driver.CoreDriver#setVariationDriver(org.ensembl.variation.driver.VariationDriver)
   */
  public void setVariationDriver(VariationDriver vdriver) throws AdaptorException {
  	
  	// prevent infinite recursion as drivers make each other aware of
  	// the other.
  	if (this.vdriver==vdriver) return;
    
  	this.vdriver = vdriver;
  	vdriver.setCoreDriver(this); // ensure drivers synchronised
  	
    // The CoordinateSystemAdaptor should include entries for the variation
    // driver, clearing it will force it to rebuild at which time it will include
  	// entries for the variation database.
  	clearAllCaches();
  }

  /* (non-Javadoc)
   * @see org.ensembl.driver.CoreDriver#getVariationDriver()
   */
  public VariationDriver getVariationDriver() {
    return vdriver;
  }
}
