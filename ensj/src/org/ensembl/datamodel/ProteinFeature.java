/*
    Copyright (C) 2002  Frans Verhoef

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

/**
 * An alignment between a part of the genome and a peptide.
 */
public interface ProteinFeature extends Locatable {
  Translation getTranslation();

  void setTranslation(Translation translation);

  /**
   * Returns translation internal id, this is the same
   * as translation.internalID if translation is available.
   *
   * @return internalID of the translation.
   */
  long getTranslationInternalID();

  /**
   * Sets translation internal id, also sets the
   * translation.internalID if translation is available.
   */
  void setTranslationInternalID(long translationInternalID);

  int getTranslationStart();

  void setTranslationStart(int translationStart);

  int getTranslationEnd();

  void setTranslationEnd(int translationEnd);

  int getPeptideEnd();

  void setPeptideEnd(int peptideEnd);

  int getPeptideStart();

  void setPeptideStart(int peptideStart);

    Analysis getAnalysis();

    void setAnalysis(Analysis analysis);

    String getDisplayName();

    void setDisplayName(String displayName);

    /**
     * @return score or Double.NaN if score not set 
     */
    double getScore();

    void setScore(double score);

    /**
     * @return evalue or Double.NaN if evalue not set 
     */
    double getEvalue();

    void setEvalue(double evalue);

    /**
     * @return percentage identity or Integer.MIN_VALUE of not set. 
     */
    double getPercentageIdentity();

    void setPercentageIdentity(double percentageIdentity);

    /**
     * Return interpro accession for this protein, can be null.
     * @return interpro accession for this protein, can be null.
     */
    String getInterproAccession();
    
    /**
     * Set the interpro accession for this protein.
     * @param interproAccession interpro accession, can be null.
     */
    void setInterproAccession(String interproAccession);
    
    /**
     * Return interpro description for this protein.
     * @return interpro description for this protein, can be null.
     */
    String getInterproDescription();
    
    /**
     * Set the interpro description for this protein.
     * @param interproDescription interpro description, can be null.
     */
    void setInterproDescription(String interproDescription);

    /**
     * Get the interpro display name.
     * @return interpro display name, can be null.
     */
    String getInterproDisplayName();
    
    /**
     * Set the interpro display name.
     * @param interproDisplayName interpro display name, can be null.
     */
    void setInterproDisplayName(String interproDisplayName);

}
