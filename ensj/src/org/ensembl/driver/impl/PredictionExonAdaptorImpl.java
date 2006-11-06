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
import org.ensembl.datamodel.PredictionExon;
import org.ensembl.datamodel.PredictionTranscript;
import org.ensembl.datamodel.impl.PredictionExonImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.LocationConverter;
import org.ensembl.driver.PredictionExonAdaptor;
import org.ensembl.driver.PredictionTranscriptAdaptor;
import org.ensembl.util.NotImplementedYetException;

// TODO implement fetch, store, delete for new API

public class 
  PredictionExonAdaptorImpl 
extends 
  BaseFeatureAdaptorImpl 
implements 
  PredictionExonAdaptor 
{

  private static final Logger logger = Logger.getLogger(PredictionExonAdaptorImpl.class.getName());

  //attempt to avoid lots of extra fetches when fetching all child prediction exons of the parent
  //transcript.
  private PredictionTranscript parentTranscript;

  public PredictionExonAdaptorImpl(CoreDriverImpl driver, String logicName, String type) {
    super(driver, logicName, type);
  }

  public PredictionExonAdaptorImpl(CoreDriverImpl driver, String[] logicNames, String type) {
    super(driver, logicNames, type);
  }

  public PredictionExonAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  protected String[] columns() {
    return new String[] {
      "prediction_exon.prediction_exon_id",
      "prediction_exon.prediction_transcript_id",
      "prediction_exon.exon_rank",
      "prediction_exon.seq_region_id",
      "prediction_exon.seq_region_start",
      "prediction_exon.seq_region_end",
      "prediction_exon.seq_region_strand",
      "prediction_exon.start_phase",
      "prediction_exon.score",
      "prediction_exon.p_value"
    };
  }


  protected String[][] tables() {
    return new String[][]{ {"prediction_exon", "prediction_exon"} };
  }

  public String[][] leftJoin() {
    return new String[][] { { } };
  }

  /**
  **/
  public Object createObject(ResultSet resultSet) throws AdaptorException {
    PredictionExonImpl predictionExon = null;
    Location location;
    Location newLocation;
    PredictionTranscriptAdaptor transcriptAdaptor;

    try {
      LocationConverter locationConverter = driver.getLocationConverter();
      
      if (resultSet.next()) {
        predictionExon = new PredictionExonImpl(getDriver());
        predictionExon.setInternalID(resultSet.getLong("prediction_exon.prediction_exon_id"));
        location =
          locationConverter.idToLocation(
            resultSet.getLong("prediction_exon.seq_region_id"),
            resultSet.getInt("prediction_exon.seq_region_start"),
            resultSet.getInt("prediction_exon.seq_region_end"),
            resultSet.getInt("prediction_exon.seq_region_strand")
          );

        newLocation = 
          getDriver().getLocationConverter().convert(
            location, 
            parentTranscript.getLocation().getCoordinateSystem()
          );

        predictionExon.setLocation(newLocation);
        predictionExon.setRank(resultSet.getInt("prediction_exon.exon_rank"));
        predictionExon.setStartPhase(resultSet.getInt("prediction_exon.start_phase"));
        predictionExon.setPvalue(resultSet.getDouble("prediction_exon.p_value"));
        predictionExon.setScore(resultSet.getDouble("prediction_exon.score"));

        predictionExon.setDriver(getDriver());
        
        if(getParentTranscript() == null){
          transcriptAdaptor = getDriver().getPredictionTranscriptAdaptor();
          parentTranscript = transcriptAdaptor.fetch(resultSet.getLong("prediction_exon.prediction_transcript_id"));
          predictionExon.setTranscript(parentTranscript);
        }else{
          predictionExon.setTranscript(getParentTranscript());
        }
      }

    } catch (InvalidLocationException exception) {
      throw new AdaptorException("Error when building Location", exception);
    } catch (SQLException exception) {
      throw new AdaptorException("SQL error when building object", exception);
    }

    return predictionExon;
  }

  public PredictionExon fetch(long internalID) throws AdaptorException {
    // just use base class method with appropriate return type
    return (PredictionExon)super.fetchByInternalID(internalID);
  }

  /**
   * Fetch all prediction exons that are the children of the input prediction transcript
  **/
  public List fetch(PredictionTranscript transcript) throws AdaptorException{
    List returnList;
    
    try{
      setParentTranscript(transcript);
      returnList = fetchByNonLocationConstraint("prediction_transcript_id = "+transcript.getInternalID());
      setParentTranscript(null);
    }finally{
      setParentTranscript(null);
    }
    
    return returnList;
  } 


  public long store(PredictionExon predictionTranscript) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }

  public void delete(long internalID) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }

  public void delete(PredictionExon predictionTranscript) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }

  void delete(Connection conn, long internalID) throws AdaptorException {
    throw new NotImplementedYetException("Not yet implemented for new API");
  }
  
  private PredictionTranscript getParentTranscript(){
    return parentTranscript;
  }
  
  private void setParentTranscript(PredictionTranscript transcript){
    parentTranscript = transcript;
  }
}
