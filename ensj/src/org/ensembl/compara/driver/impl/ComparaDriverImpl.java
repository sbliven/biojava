package org.ensembl.compara.driver.impl;

import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;

import org.ensembl.compara.datamodel.ComparaDataFactory;
import org.ensembl.compara.datamodel.impl.ComparaDataFactoryImpl;
import org.ensembl.compara.driver.ComparaDriver;
import org.ensembl.compara.driver.DnaDnaAlignFeatureAdaptor;
import org.ensembl.compara.driver.DnaFragmentAdaptor;
import org.ensembl.compara.driver.GenomeDBAdaptor;
import org.ensembl.compara.driver.GenomicAlignAdaptor;
import org.ensembl.compara.driver.GenomicAlignBlockAdaptor;
import org.ensembl.compara.driver.HomologyAdaptor;
import org.ensembl.compara.driver.MemberAdaptor;
import org.ensembl.compara.driver.MethodLinkAdaptor;
import org.ensembl.compara.driver.MethodLinkSpeciesSetAdaptor;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.ConfigurationException;

/**
 * This driver behaves exactly like the "standard" core CoreDriverImpl, with the
 * exception that it deals with adapters and objects over the 
 * ensembl compara databases.
 * @see org.ensembl.driver.impl.CoreDriverImpl
 */
public class ComparaDriverImpl 
  extends org.ensembl.driver.impl.CoreDriverImpl implements ComparaDriver {

  private static final Logger logger =
    Logger.getLogger(ComparaDriverImpl.class.getName());
  protected ComparaDataFactory factory = null;

  public ComparaDriverImpl(){
  }
  
  public ComparaDriverImpl(Properties config) throws AdaptorException {
    initialise(config);
  }

  /**
   * Create adaptors and store reference to them to enable access via driver
   * in future.
   */
  protected void loadAdaptors()
    throws AdaptorException, ConfigurationException {

    super.loadAdaptors();    
    
    factory = new ComparaDataFactoryImpl(this);

    Connection testConnection = getConnection();

    // Quit with warning if database not available. In this case the driver
    // will have be NO adaptors.
    try {
      testConnection.close();
    } catch (Exception e) {
      throw new AdaptorException("Failed to connect to database", e);
    }

    addAdaptor(new GenomeDBAdaptorImpl(this));

    addAdaptor(new DnaFragmentAdaptorImpl(this));

    addAdaptor(new GenomicAlignAdaptorImpl(this));

    addAdaptor(new DnaDnaAlignFeatureAdaptorImpl(this));

    addAdaptor(new HomologyAdaptorImpl(this));

    addAdaptor(new MethodLinkAdaptorImpl(this));

    addAdaptor(new MethodLinkSpeciesSetAdaptorImpl(this));

    addAdaptor(new GenomicAlignBlockAdaptorImpl(this));

    addAdaptor(new MemberAdaptorImpl(this));
    // Configure adaptors loaded so far.
    Adaptor[] as = getAdaptors();
    for (int i = 0; i < as.length; i++) {
      Object o = as[i];
      if (o instanceof ComparaBaseAdaptor)
         ((ComparaBaseAdaptor) o).configure();

    } //end while

  } //end loadAdaptors

  
  
  
  public DnaDnaAlignFeatureAdaptor getDnaDnaAlignFeatureAdaptor()
      throws AdaptorException {
    return (DnaDnaAlignFeatureAdaptor) getAdaptor(DnaDnaAlignFeatureAdaptor.TYPE);
  }
  
  public DnaFragmentAdaptor getDnaFragmentAdaptor() throws AdaptorException {
    return (DnaFragmentAdaptor) getAdaptor(DnaFragmentAdaptor.TYPE);
  }

  public GenomeDBAdaptor getGenomeDBAdaptor() throws AdaptorException {
    return (GenomeDBAdaptor) getAdaptor(GenomeDBAdaptor.TYPE);
  }

  public GenomicAlignAdaptor getGenomicAlignAdaptor() throws AdaptorException {
    return (GenomicAlignAdaptor) getAdaptor(GenomicAlignAdaptor.TYPE);
  }

  public GenomicAlignBlockAdaptor getGenomicAlignBlockAdaptor()
      throws AdaptorException {
    return (GenomicAlignBlockAdaptor) getAdaptor(GenomicAlignBlockAdaptor.TYPE);
  }

  public HomologyAdaptor getHomologyAdaptor() throws AdaptorException {
    return (HomologyAdaptor) getAdaptor(HomologyAdaptor.TYPE);
  }

  public MemberAdaptor getMemberAdaptor() throws AdaptorException {
    return (MemberAdaptor) getAdaptor(MemberAdaptor.TYPE);
  }

  public MethodLinkAdaptor getMethodLinkAdaptor() throws AdaptorException {
    return (MethodLinkAdaptor) getAdaptor(MethodLinkAdaptor.TYPE);
  }
  
  public MethodLinkSpeciesSetAdaptor getMethodLinkSpeciesSetAdaptor() throws AdaptorException {
    return (MethodLinkSpeciesSetAdaptor) getAdaptor(MethodLinkSpeciesSetAdaptor.TYPE);
  }  
}
