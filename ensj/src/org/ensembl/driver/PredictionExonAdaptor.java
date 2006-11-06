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
package org.ensembl.driver;
import java.util.List;

import org.ensembl.datamodel.PredictionExon;
import org.ensembl.datamodel.PredictionTranscript;

public interface PredictionExonAdaptor extends FeatureAdaptor{
  
  final static String TYPE = "prediction_exon";
  
  PredictionExon fetch(long internalID) throws AdaptorException;

  long store( PredictionExon predictionTranscript ) throws AdaptorException;

  void delete(long internalID) throws AdaptorException;

  void delete(PredictionExon predictionTranscript) throws AdaptorException;
  
  List fetch(PredictionTranscript transcript) throws AdaptorException;
}
