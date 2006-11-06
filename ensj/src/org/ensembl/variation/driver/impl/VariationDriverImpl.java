/*
    Copyright (C) 2001 EBI, GRL

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.variation.driver.impl;

import java.util.Properties;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.ConfigurationException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.impl.EnsemblDriverImpl;
import org.ensembl.variation.datamodel.AlleleConsequenceAdaptor;
import org.ensembl.variation.driver.AlleleFeatureAdaptor;
import org.ensembl.variation.driver.AlleleGroupAdaptor;
import org.ensembl.variation.driver.IndividualAdaptor;
import org.ensembl.variation.driver.IndividualGenotypeAdaptor;
import org.ensembl.variation.driver.LDFeatureAdaptor;
import org.ensembl.variation.driver.PopulationAdaptor;
import org.ensembl.variation.driver.PopulationGenotypeAdaptor;
import org.ensembl.variation.driver.TranscriptVariationAdaptor;
import org.ensembl.variation.driver.VariationAdaptor;
import org.ensembl.variation.driver.VariationDriver;
import org.ensembl.variation.driver.VariationFeatureAdaptor;
import org.ensembl.variation.driver.VariationGroupAdaptor;
import org.ensembl.variation.driver.VariationGroupFeatureAdaptor;

/**
 * This driver provides access to data in 
 * Ensembl variation databases.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class VariationDriverImpl
  extends EnsemblDriverImpl
  implements VariationDriver {

  private VariationGroupAdaptor variationGroupAdaptor;

  private AlleleGroupAdaptor alleleGroupAdaptor;

  private PopulationAdaptor populationAdaptor;

  private VariationAdaptor variationAdaptor;

  private VariationFeatureAdaptor variationFeatureAdaptor;

  private CoreDriver coreDriver;

  private IndividualAdaptor individualAdaptor;

private IndividualGenotypeAdaptor individualGenotypeAdaptor;

private PopulationGenotypeAdaptor populationGenotypeAdaptor;

private TranscriptVariationAdaptor transcriptVariationAdaptor;

private VariationGroupFeatureAdaptor variationGroupFeatureAdaptor;

private LDFeatureAdaptor lDFeatureAdaptor;

private AlleleFeatureAdaptor alleleFeatureAdaptor;

private AlleleConsequenceAdaptor alleleConsequenceAdaptor;

  /**
   * @throws AdaptorException
   */
  public VariationDriverImpl() throws AdaptorException {
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
  public VariationDriverImpl(Properties configuration) throws AdaptorException {
    super(configuration, true);
  }  
  
  /**
   * @param host
   * @param database
   * @param user
   * @throws AdaptorException
   */
  public VariationDriverImpl(String host, String database, String user)
    throws AdaptorException {
    super(host, database, user, false);
  }

  /**
   * @param host
   * @param database
   * @param user
   * @param password
   * @throws AdaptorException
   */
  public VariationDriverImpl(
    String host,
    String database,
    String user,
    String password)
    throws AdaptorException {
    super(host, database, user, password, false);
  }

  /**
   * @param host
   * @param database
   * @param user
   * @param password
   * @param port
   * @throws AdaptorException
   */
  public VariationDriverImpl(
    String host,
    String database,
    String user,
    String password,
    String port)
    throws AdaptorException {
    super(host, database, user, password, port, false);
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
  public VariationDriverImpl(String host, String database, String user,
      String password, String port, boolean databaseIsPrefix)
      throws AdaptorException {
    super(host, database, user, password, port, databaseIsPrefix);
  }
  


  /**
   * Does nothing because the adaptors are created on demand.
   */
  protected void loadAdaptors()
    throws AdaptorException, ConfigurationException {
    super.loadAdaptors();
  }

  /**
   * @see org.ensembl.variation.driver.VariationDriver#getVariationFeatureAdaptor()
   */
  public VariationFeatureAdaptor getVariationFeatureAdaptor()
    throws AdaptorException {

    if (variationFeatureAdaptor == null)
      addAdaptor(
        variationFeatureAdaptor = new VariationFeatureAdaptorImpl(this));

    return variationFeatureAdaptor;

  }

  /**
   * @see org.ensembl.variation.driver.VariationDriver#getVariationAdaptor()
   */
  public VariationAdaptor getVariationAdaptor() throws AdaptorException {

    if (variationAdaptor == null)
      addAdaptor(variationAdaptor = new VariationAdaptorImpl(this));

    return variationAdaptor;
  }

  /**
   * @see org.ensembl.variation.driver.VariationDriver#getPopulationAdaptor()
   */
  public PopulationAdaptor getPopulationAdaptor() throws AdaptorException {

    if (populationAdaptor == null)
      addAdaptor(populationAdaptor = new PopulationAdaptorImpl(this));

    return populationAdaptor;
  }


  /**
   * @see org.ensembl.variation.driver.VariationDriver#getVariationGroupAdaptor()
   */
  public VariationGroupAdaptor getVariationGroupAdaptor() throws AdaptorException {

    if (variationGroupAdaptor == null)
      addAdaptor(variationGroupAdaptor = new VariationGroupAdaptorImpl(this));

    return variationGroupAdaptor;
  }


  /**
   * @see org.ensembl.variation.driver.VariationDriver#getAlleleGroupAdaptor()
   */
  public AlleleGroupAdaptor getAlleleGroupAdaptor() throws AdaptorException {

    if (alleleGroupAdaptor == null)
      addAdaptor(alleleGroupAdaptor = new AlleleGroupAdaptorImpl(this));

    return alleleGroupAdaptor;
  }
  

  /**
   * @see org.ensembl.variation.driver.VariationDriver#getIndividualAdaptor()
   */
  public IndividualAdaptor getIndividualAdaptor() throws AdaptorException {

    if (individualAdaptor == null)
      addAdaptor(individualAdaptor = new IndividualAdaptorImpl(this));

    return individualAdaptor;
  }
  
  /**
   * @see org.ensembl.variation.driver.VariationDriver#getCoreDriver()
   */
  public CoreDriver getCoreDriver() {
    return coreDriver;
  }

  /**
   * @see org.ensembl.variation.driver.VariationDriver#setCoreDriver(CoreDriver coreDriver)
   */
  public void setCoreDriver(CoreDriver coreDriver) throws AdaptorException {
  	
    // prevent infinite recursion as drivers make each other aware of
  	// the other.
  	if (this.coreDriver==coreDriver) return;
  	
    this.coreDriver = coreDriver;
    // the coordinate system adaptor in core needs to 
    // be able to see the meta_coord table in the variation
    // database to discover which coordinate systems 
    // variation related types are stored in.
    coreDriver.setVariationDriver(this);
    
  }

/**
 * @see org.ensembl.variation.driver.VariationDriver#getIndividualGenotypeAdaptor()
 */
public IndividualGenotypeAdaptor getIndividualGenotypeAdaptor() throws AdaptorException {
	if (individualGenotypeAdaptor == null)
    addAdaptor(individualGenotypeAdaptor = new IndividualGenotypeAdaptorImpl(this));

	return individualGenotypeAdaptor;
}

/**
 * @see org.ensembl.variation.driver.VariationDriver#getPopulationGenotypeAdaptor()
 */
public PopulationGenotypeAdaptor getPopulationGenotypeAdaptor() throws AdaptorException {
	if (populationGenotypeAdaptor == null)
    addAdaptor(populationGenotypeAdaptor = new PopulationGenotypeAdaptorImpl(this));

	return populationGenotypeAdaptor;
}

/**
 * @see org.ensembl.variation.driver.VariationDriver#getTranscriptVariationAdaptor()
 */
public TranscriptVariationAdaptor getTranscriptVariationAdaptor() throws AdaptorException {
	if (transcriptVariationAdaptor == null)
    addAdaptor(transcriptVariationAdaptor = new TranscriptVariationAdaptorImpl(this));

	return transcriptVariationAdaptor;
}

/* (non-Javadoc)
 * @see org.ensembl.variation.driver.VariationDriver#getVariationGroupFeatureAdaptor()
 */
public VariationGroupFeatureAdaptor getVariationGroupFeatureAdaptor() throws AdaptorException {
  if (variationGroupFeatureAdaptor == null)
    addAdaptor(variationGroupFeatureAdaptor = new VariationGroupFeatureAdaptorImpl(this));

  return variationGroupFeatureAdaptor;
}

/**
 * @throws AdaptorException
 * @see org.ensembl.variation.driver.VariationDriver#getLDFeatureAdaptor()
 */
public LDFeatureAdaptor getLDFeatureAdaptor() throws AdaptorException {
	if (lDFeatureAdaptor == null)
    addAdaptor(lDFeatureAdaptor = new LDFeatureAdaptorImpl(this));

  return lDFeatureAdaptor;
}


/**
 * @throws AdaptorException
 * @see org.ensembl.variation.driver.VariationDriver#getAlleleFeatureAdaptor()
 */
public AlleleFeatureAdaptor getAlleleFeatureAdaptor() throws AdaptorException {
	if (alleleFeatureAdaptor == null)
    addAdaptor(alleleFeatureAdaptor = new AlleleFeatureAdaptorImpl(this));

  return alleleFeatureAdaptor;
}

/**
 * @throws AdaptorException
 * @see org.ensembl.variation.driver.VariationDriver#getAlleleConsequenceAdaptor()
 */
public AlleleConsequenceAdaptor getAlleleConsequenceAdaptor() throws AdaptorException {
	if (alleleConsequenceAdaptor == null)
    addAdaptor(alleleConsequenceAdaptor = new AlleleConsequenceAdaptorImpl(this));

  return alleleConsequenceAdaptor;
}
}
