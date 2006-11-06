/*
  Copyright (C) 2003 EBI, GRL

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
package org.ensembl.driver.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.PredictionTranscript;
import org.ensembl.datamodel.impl.PredictionTranscriptImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.AnalysisAdaptor;
import org.ensembl.driver.LocationConverter;
import org.ensembl.driver.PredictionTranscriptAdaptor;
import org.ensembl.util.NotImplementedYetException;

// TODO implement fetch, store, delete for new API

public class 
  PredictionTranscriptAdaptorImpl 
extends 
  BaseFeatureAdaptorImpl 
implements 
  PredictionTranscriptAdaptor 
{
  private List predictionExons;
 
  private static final Logger logger = Logger.getLogger(PredictionTranscriptAdaptorImpl.class.getName());

  public PredictionTranscriptAdaptorImpl(CoreDriverImpl driver, String logicName, String type) {
    super(driver, logicName, type);
  }

  public PredictionTranscriptAdaptorImpl(CoreDriverImpl driver, String[] logicNames, String type) {
    super(driver, logicNames, type);
  }

  public PredictionTranscriptAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  protected String[] columns() {
    return new String[] {
      "prediction_transcript_id",
      "seq_region_id",
      "seq_region_start",
      "seq_region_end",
      "seq_region_strand",
      "analysis_id"
    };
  }


  protected String[][] tables() {
    return new String[][]{
      {"prediction_transcript", "prediction_transcript"}
    };
  }

  public String[][] leftJoin() {
    return new String[][] { { } };
  }

  /**
   * Create a PredictionTranscript with no PredictionExons - fill the exons in
   * when they're asked for, with the getAllExons call. 
  **/
  public Object createObject(ResultSet resultSet) throws AdaptorException {
    PredictionTranscriptImpl predictionTranscript = null;
    Location location;

    try {
      LocationConverter locationConverter = driver.getLocationConverter();
      AnalysisAdaptor analysisAdaptor = driver.getAnalysisAdaptor();
      if (resultSet.next()) {
        
        predictionTranscript = new PredictionTranscriptImpl(getDriver());
        predictionTranscript.setInternalID(resultSet.getLong("prediction_transcript_id"));
        location =
          locationConverter.idToLocation(
            resultSet.getLong("seq_region_id"),
            resultSet.getInt("seq_region_start"),
            resultSet.getInt("seq_region_end"),
            resultSet.getInt("seq_region_strand")
          );

        predictionTranscript.setLocation(location);
        predictionTranscript.setAnalysisID(resultSet.getLong("analysis_id"));

        predictionTranscript.setDriver(getDriver());
      }

    } catch (InvalidLocationException exception) {
      exception.printStackTrace();
      throw new AdaptorException("Error when building Location", exception);
    } catch (SQLException exception) {
      exception.printStackTrace();
      throw new AdaptorException("SQL error when building object", exception);
    }catch(Throwable e){
      e.printStackTrace();
    }

    return predictionTranscript;
  }

  public PredictionTranscript fetch(long internalID) throws AdaptorException {
    // just use base class method with appropriate return type
    return (PredictionTranscript)super.fetchByInternalID(internalID);
  }

  public long store(PredictionTranscript predictionTranscript) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }

  public void delete(long internalID) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }

  public void delete(PredictionTranscript predictionTranscript) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }

  void delete(Connection conn, long internalID) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }

  public List fetch(String logicalName) throws AdaptorException {
    
    throw new NotImplementedYetException("Not yet implemented");
  }
}
