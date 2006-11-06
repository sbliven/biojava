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
package org.ensembl.datamodel.impl;

import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.ExternalDatabase;
import org.ensembl.datamodel.ExternalRef;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.ExternalDatabaseAdaptor;
import org.ensembl.driver.RuntimeAdaptorException;


public class ExternalRefImpl extends PersistentImpl implements ExternalRef {

  private static final Logger logger = Logger.getLogger( ExternalRefImpl.class.getName() );
  private static final long serialVersionUID = 1L;

  private List synonyms;
  private String description;
  private String displayID;
  private String primaryID;
  private String version;
  private long externalDbId;
  private String goLinkageType;
  private String infoText;
  private String infoType;
  private long queryInternalID;
  private int targetIdentity;
  private int queryIdentity;
  private ExternalDatabase externalDatabase;
  private long objectXrefID;

  
  public ExternalRefImpl(CoreDriver driver) {
    super( driver );
  }
  
  public List getSynonyms(){
	  if (synonyms==null)
      try {
        driver.getExternalRefAdaptor().fetchCompleteSynonyms(this);
      } catch (AdaptorException e) {
        throw new RuntimeAdaptorException(e);
      }
    return synonyms;
  }
  
  
  public String getDescription(){
    return description;
  }
  
  public void setDescription(String description){
    this.description = description;
  }
  
  public String getDisplayID(){
    return displayID;
  }
  
  public void setDisplayID(String displayID){
    this.displayID = displayID;
  }
  
  public String getPrimaryID(){
    return primaryID;
  }
  
  public void setPrimaryID(String primaryID){
    this.primaryID = primaryID;
  }
  
  public String getVersion(){
    return version;
  }
  
  public void setVersion(String version){
    this.version = version;
  }
  
  public void setSynonyms(List synonyms){
    this.synonyms = synonyms;
  }
  

  
  public long getQueryInternalID(){ return queryInternalID; }
  
  public void setQueryInternalID(long queryInternalID){ this.queryInternalID = queryInternalID; }
  
  public int getTargetIdentity(){ 
    if (targetIdentity<1)
      getQueryIdentity(); // lazy load targetIdenty 
    return targetIdentity; }
  
  public void setTargetIdentity(int targetIdentity){ this.targetIdentity = targetIdentity; }
  
  
  public int getQueryIdentity(){ 
  if (queryIdentity<1)
    try {
      driver.getExternalRefAdaptor().fetchCompleteIdentity(this);
    } catch (AdaptorException e) {
      throw new RuntimeAdaptorException(e);
    }
    return queryIdentity; 
  }
  
  public void setQueryIdentity(int queryIdentity){ this.queryIdentity = queryIdentity; }
  
  
  
  public long getExternalDbId() {
    if (externalDatabase!=null )
      externalDbId = externalDatabase.getInternalID();
    return externalDbId;
  }
  
  public void setExternalDbId(long externalDbId) {
    this.externalDbId = externalDbId;
  }
  
  
  public ExternalDatabase getExternalDatabase(){
    if ( externalDatabase==null && externalDbId != -1 && driver!=null ) lazyLoadExternalDatabase();
    if ( externalDatabase==null ) {
      externalDbId = -1;
    }
    return externalDatabase;
  }
  
  public void setExternalDatabase(ExternalDatabase externalDatabase){ this.externalDatabase = externalDatabase; }
  
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append("{").append(super.toString()).append("}, ");
    buf.append("externalDbId_id=").append(getExternalDbId()).append(", ");
    buf.append("dbprimary_id=").append(getPrimaryID()).append(", ");
    buf.append("display_id=").append(getDisplayID()).append(", ");
    buf.append("version=").append(getVersion()).append(", ");
    buf.append("description=").append(getDisplayID()).append(", ");
    buf.append("synonyms=").append(synonyms);
    buf.append("]");
    
    return buf.toString();
  }
  
  private void lazyLoadExternalDatabase() {
    try {
      // Load a new database with same internalID
      ExternalDatabaseAdaptor xDbAdaptor = (ExternalDatabaseAdaptor)driver.getAdaptor("external_database");
      externalDatabase = xDbAdaptor.fetch(this.externalDbId);
    } catch (AdaptorException e) {
      logger.warning(e.getMessage());
      externalDbId = -1;
    }
  }
  
  /**
   * Get the GO linkage type.
   * @return The linkage type if this is a go_xref. If not, an empty string is returned.
   */
  public String getGoLinkageType() {
    
    if (goLinkageType==null)
      try {
        driver.getExternalRefAdaptor().fetchCompleteGoLinkageType(this);
      } catch (AdaptorException e) {
        throw new RuntimeAdaptorException(e);
      }
    return goLinkageType;
    
  }
  
  /**
   * Set the GO linkage type.
   * @param linkageType The new linkage type.
   */
  public void setGoLinkageType(String linkageType) {
    
    goLinkageType = linkageType;
    
  }
  
  
 
  
  public String getInfoText() {
    return infoText;
  }

  public String getInfoType() {
    return infoType;
  }

  public void setInfoText(String text) {
    this.infoText = text;
  }

  public void setInfoType(String type) {
    this.infoType = type;
  }

  public void setObjectXrefID(long objectXrefID) {
    this.objectXrefID = objectXrefID;
  }

  public long getObjectXrefID() {
    return objectXrefID;
  }
}

