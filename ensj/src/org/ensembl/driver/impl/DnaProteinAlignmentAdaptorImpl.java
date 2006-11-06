package org.ensembl.driver.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.DnaProteinAlignment;
import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.impl.DnaProteinAlignmentImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.AnalysisAdaptor;
import org.ensembl.driver.DnaProteinAlignmentAdaptor;
import org.ensembl.driver.LocationConverter;
import org.ensembl.util.NotImplementedYetException;

// TODO Implement fetch/store/delete for new schema/API

public class
      DnaProteinAlignmentAdaptorImpl
extends
      BaseFeatureAdaptorImpl
implements
      DnaProteinAlignmentAdaptor
{
  private static final Logger logger = Logger.getLogger(DnaProteinAlignmentAdaptorImpl.class.getName());


  public DnaProteinAlignmentAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  public DnaProteinAlignmentAdaptorImpl(CoreDriverImpl driver, String logicName, String type) {
    super(driver, logicName, type);
  }

  public DnaProteinAlignmentAdaptorImpl(CoreDriverImpl driver, String[] logicNames, String type) {
    super(driver, logicNames, type);
  }

  protected String[] columns() {
    return new String[]{
             "protein_align_feature_id",
             "seq_region_id",
             "seq_region_start",
             "seq_region_end",
             "seq_region_strand",
             "hit_start",
             "hit_end",
             "hit_name",
             "analysis_id",
             "score",
             "evalue",
             "perc_ident",
             "cigar_line"
           };
  } // columns

  protected String[][] tables() {
    return new String[][] {{"protein_align_feature","protein_align_feature"}};
  }

  public Object createObject(ResultSet resultSet) throws AdaptorException {
    DnaProteinAlignmentImpl align = null;
    Location location;
    CoordinateSystem myCoordinateSystem;

    try {
      LocationConverter locationConverter = driver.getLocationConverter();
      AnalysisAdaptor analysisAdaptor = driver.getAnalysisAdaptor();
      if (resultSet.next()) {
        align = new DnaProteinAlignmentImpl(getDriver());
        align.setInternalID(resultSet.getLong("protein_align_feature_id"));
        location = new Location(
            resultSet.getLong("seq_region_id"),
            resultSet.getInt("seq_region_start"),
            resultSet.getInt("seq_region_end"),
            resultSet.getInt("seq_region_strand")
          );

        align.setLocation(location);
        align.setScore(resultSet.getDouble("score"));
        align.setAnalysisID(resultSet.getLong("analysis_id"));

        align.setHitAccession(resultSet.getString("hit_name"));
        align.setHitDisplayName(align.getHitAccession());
        align.setHitDescription(align.getHitAccession());
        align.setHitLocation(
          new Location(
            "dnaProteinAlignment", 
            align.getHitAccession(),
            resultSet.getInt("hit_start"),
            resultSet.getInt("hit_end"),
            0
          )
        );

        align.setPercentageIdentity(resultSet.getDouble("perc_ident"));
        align.setEvalue(resultSet.getDouble("evalue"));
        align.setScore(resultSet.getDouble("score"));
        align.setCigarString(resultSet.getString("cigar_line"));
        align.setDriver(getDriver());
      }

    } catch (InvalidLocationException exception) {
      throw new AdaptorException("Error when building Location", exception);
    } catch (SQLException exception) {
      throw new AdaptorException("SQL error when building object", exception);
    }

    return align;
  }

  /**
   * @param internalID The id of the DnaProteinAlignment to be fetched
   * @return A feature matching the internalID, or null if non found.
   */
  public DnaProteinAlignment fetch(long internalID) throws AdaptorException {

    return (DnaProteinAlignment) fetchByInternalID(internalID);

  }

  /**
   * Warning: this is potentially a very slow query.
   * @return list of DnaProteinAlignments corresponding to the specified analysis
   */
  public List fetch(String logicalName) throws AdaptorException {
    return fetchByNonLocationConstraint( getAnalysisIDCondition(logicalName) );
  }
  
  public Iterator fetchIterator(String logicalName) throws AdaptorException {
  	return fetchIteratorBySQL( "SELECT protein_align_feature_id " +
  			"FROM protein_align_feature paf, analysis a " +
  			"WHERE paf.analysis_id=a.analysis_id AND logic_name='"+logicalName+"'" );
  }
  
  public long store(DnaProteinAlignment feature) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }

  public void delete(DnaProteinAlignment feature) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented for new API");

  }

  public void delete(long internalID) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented for new API");

  }

  
}
