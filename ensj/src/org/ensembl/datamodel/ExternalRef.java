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


package org.ensembl.datamodel;

import java.util.List;


/**
 * Reference to object outside ensembl database. 
 */
public interface ExternalRef extends Persistent  {

    /** Gene type */
    final static int GENE = 0;
    /** Translation type */
    final static int TRANSLATION = 1;
    /** Transcript type. */
	final static int TRANSCRIPT = 2;
    /** Clone fragment type. */
	final static int CLONE_FRAGMENT= 3;

   /** @link dependency */
   /*# ExternalDatabase lnkExternalDatabase; */

   void setSynonyms(List synonyms);

   List getSynonyms();

   String getDescription();

   void setDescription(String description);

   String getDisplayID();

   void setDisplayID(String displayID);

   String getPrimaryID();

   void setPrimaryID(String primaryID);

   String getVersion();

   void setVersion(String version);

   void setObjectXrefID(long objectXrefID);

   long getObjectXrefID();

   
//    /**
//     * Type of reference; one of ExternalRef.GENE, ExternalRef.TRANSCRIPT,
//     * ExternalRef.TRANSLATION or ExternalRef.CLONE_FRAGMENT.
//     */
//    int getType();

//    /**
//     * @param type Type of reference, should be one of ExternalRef.GENE, ExternalRef.TRANSCRIPT, ExternalRef.TRANSLATION or ExternalRef.CLONE_FRAGMENT.
//     */
//    void setType(int type);

   /**
    * Internal ID of the thing this is an external reference for.
    */
   long getQueryInternalID();

   void setQueryInternalID(long queryInternalID);

   /**
    * Percentage of the external object included in an overlapping region with
    * the ensembl object.
    */
   int getTargetIdentity();

   void setTargetIdentity(int targetIdentity);

   /**
    * Percentage of the ensembl object included in an overlapping region with
    * the external object.
    */
   int getQueryIdentity();

   void setQueryIdentity(int queryIdentity);

  ExternalDatabase getExternalDatabase();
  
  void setExternalDatabase(ExternalDatabase externalDatabase);
  
  long getExternalDbId();
  
  void setExternalDbId(long externalDbId);
  
  /**
   * Set the linkage_type column on the go_xref table if this is a GO xref.
   */
   void setGoLinkageType(String linkageType);
   
   /**
    * Get the linkage_type column on the go_xref table if this is a GO xref.
    */
   String getGoLinkageType();
   
   
   /**
    * Info type for the xref.
    * 
    * For example type is PROJECTION for an xref projected from another
    * database.
    * 
    * @return info type. Can be null.
    */
   String getInfoType();
   
   /**
    * Set the Info type for the xref.
    * 
    * For example type is PROJECTION for an xref projected from another
    * database.
    * 
    * @param type info type. Can be null.
    */
   void setInfoType(String type);

   /**
    * Info text for the xref.
    * 
    * For example the source database this xref was projected from.
    * 
    * @return info type. Can be null.
    */
   String getInfoText();
   
   /**
    * Set the Info text for the xref.
    * 
    * @param text info text. Can be null.
    */
   void setInfoText(String text);   
}
