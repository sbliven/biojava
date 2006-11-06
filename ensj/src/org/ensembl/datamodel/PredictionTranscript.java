/*
    Copyright (C) 2002 EBI, GRL

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


package org.ensembl.datamodel;

import java.util.List;

/**
 * Transcript consisting of PredictionExons. 
 *
 * @see PredictionExon PredictionExon
 */
public interface PredictionTranscript extends Feature {
  /**
  * List of PredictionExons.
  */
  List getExons();

  void setExons(List exons);

  /**
   * Analysis which created this prediction.
   */
  int getExonCount();

  /**
   * Number of exons.
   */
  void setExonCount(int exonCount);

  /**
   * Analysis which created this prediction.
   */
  Analysis getAnalysis();

  /**
   * Analysis which created this prediction.
   */
  void setAnalysis(Analysis analysis);

 /**
  * Translate the prediction transcript
  */
  String translate();
}
