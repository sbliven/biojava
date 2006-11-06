package org.ensembl.driver.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.DnaDnaAlignment;
import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.impl.DnaDnaAlignmentImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.DnaDnaAlignmentAdaptor;
import org.ensembl.util.NotImplementedYetException;

public class
      DnaDnaAlignmentAdaptorImpl
      extends
      BaseFeatureAdaptorImpl
      implements
      DnaDnaAlignmentAdaptor
{

  private static final Logger logger = Logger.getLogger(DnaDnaAlignmentAdaptorImpl.class.getName());


  /**
   * Constructs an adaptor capable of retrieving all DnaDnaAlignments from
   * the db table "dna_align_feature".
   */
  public DnaDnaAlignmentAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  /**
   * Constructs an adaptor capable of retrieving all DnaDnaAlignments from
   * the db table "dna_align_feature" with the specified analysis logicName.
   */
  public DnaDnaAlignmentAdaptorImpl(CoreDriverImpl driver, String logicName, String type) {
    super(driver, logicName, type);
  }

  /**
   * Constructs an adaptor capable of retrieving all DnaDnaAlignments from
   * the db table "dna_align_feature" with the specified analysis logicNames.
   */
  public DnaDnaAlignmentAdaptorImpl(CoreDriverImpl driver, String[] logicNames, String type) {
    super(driver, logicNames, type);
  }

  protected String[] columns() {

    return
      new String[]{
        "dna_align_feature_id",
        "seq_region_id",
        "seq_region_start",
        "seq_region_end",
        "seq_region_strand",
        "hit_start",
        "hit_end",
        "hit_strand",
        "hit_name",
        "analysis_id",
        "score",
        "evalue",
        "perc_ident",
        "cigar_line"
      };

  } // columns

  protected String[][] tables() {
    return new String[][] {{"dna_align_feature","dna_align_feature"}};
  }

  public Object createObject(ResultSet resultSet) throws AdaptorException {

    DnaDnaAlignmentImpl align = null;
    Location location;
    CoordinateSystem myCoordinateSystem;
    //LocationConverter locationConverter = driver.getLocationConverter();
    try {
      if (resultSet.next()) {

        align = new DnaDnaAlignmentImpl(getDriver());

        align.setInternalID(resultSet.getLong("dna_align_feature_id"));
        
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
        myCoordinateSystem = location.getCoordinateSystem();
        align.setHitLocation(
          new Location(
            "dnaAlignment",
            align.getHitAccession(),
            resultSet.getInt("hit_start"),
            resultSet.getInt("hit_end"),
            resultSet.getInt("hit_strand")
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

  public List fetch(String logicalName) throws AdaptorException {
    return fetchByNonLocationConstraint(getAnalysisIDCondition(logicalName));
  }
  
  public DnaDnaAlignment fetch(long internalID) throws AdaptorException {
    return (DnaDnaAlignment)super.fetchByInternalID(internalID);
  }

  public long store(DnaDnaAlignment feature) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }

  public void delete(DnaDnaAlignment feature) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }

  public void delete(long internalID) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }


}
