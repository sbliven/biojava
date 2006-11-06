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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ensembl.datamodel.MappingSession;
import org.ensembl.datamodel.StableIDEvent;


public class StableIDEventImpl extends PersistentImpl 
  implements StableIDEvent, Comparable {

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;


  	
 private static final Logger logger =
				Logger.getLogger(StableIDEventImpl.class.getName());

  /**
   * 
   */
  private Set relatedStableIDs = new HashSet();
  private boolean created = false;
  private boolean deleted = false;
  private String stableID;
  private String type = null;
  private int stableIDVersion = 0;
  
  
  
  
  public void setType(String type) {
	  this.type = type;
  }	

  /**
   * @return the type of the stable ID, one of the constants
   * StableIDEvent.GENE, StableIDEvent.TRANSCRIPT ,StableIDEvent.TRANSLATION
   * ,StableIDEvent.UNKOWN
   */
  public String getType() {
    
    return type;
  }

  /**
   * Orders by stableID.
   */
  public int compareTo( Object o ) {
    StableIDEvent other = (StableIDEvent)o;
    return stableID.compareTo( other.getStableID() );
  }
  

  public void setSession(MappingSession session){ this.session = session; }

  private MappingSession session;
  
  /** maps related stable id names to their relevant versions. */
  private Map related2Versions = new HashMap();

  public MappingSession getSession(){ return session; }

  public String getStableID(){ return stableID; }

  /**
   * If relatedStableID is null then it is not added but as
   * a side effect deleted is set to true.
   * @return whether relatedStableID was added.
   */
  public boolean addRelated(String relatedStableID, int version) {
  	
    if ( relatedStableID==null ) {
      deleted = true;
      return false;
    }
    else {
    	Set versions = (Set) related2Versions.get( relatedStableID );
    	if ( versions==null ) {
    		versions = new HashSet();
    		related2Versions.put( relatedStableID, versions );  
    	}
    	versions.add( new Integer( version ) );
      return relatedStableIDs.add( relatedStableID );
    }
  }


  /**
   * @return whether relatedStableID was removed.
   */
  public boolean removeRelated(String relatedStableID) {
    return relatedStableIDs.remove( relatedStableID );
  } 

  public Set getRelatedStableIDs(){ return relatedStableIDs; }



	public int[] getRelatedVersions(String relatedStableID) {

		int[] versionArr = new int[0];		
		Set versions = (Set) related2Versions.get( relatedStableID );
		
		if ( versions!=null && versions.size()>0 ) {
			versionArr = new int[ versions.size() ];
			int i= 0;
			for (Iterator iter = versions.iterator(); iter.hasNext();) {
				Integer version = (Integer) iter.next();
				versionArr[ i++ ] = version.intValue();
			}
		}
		Arrays.sort( versionArr );
		return versionArr;
	}


  public void setStableID(String stableID){ 
    this.stableID = stableID; 
  }

  public int getStableIDVersion() {
	  return stableIDVersion;
  }

  public void setStableIDVersion(int stableIDVersion) {
	  this.stableIDVersion = stableIDVersion;
  }


  public boolean isCarried(){
    return relatedStableIDs.contains( stableID );
  }

  public boolean isSplit(){
    return relatedStableIDs.size()>1;
  }

  public boolean isMerged(){
    return relatedStableIDs.size()>0 && !relatedStableIDs.contains( stableID );
  }

  public boolean isCreated(){
    return created;
  }

  public boolean isDeleted(){
    return deleted;
  }


  public void setDeleted( boolean deleted ) {
    this.deleted = deleted;
  }

  public void setCreated( boolean created ) {
    this.created = created;
  }

  
  public String toString() {
    StringBuffer buf = new StringBuffer();
    
    buf.append("[");
    buf.append("stableID=").append(stableID).append(", ");
    buf.append("relatedStableIDs=").append(relatedStableIDs).append(", ");
    buf.append("carried=").append( isCarried() ).append(", ");
    buf.append("split=").append( isSplit() ).append(", ");
    buf.append("merged=").append( isMerged() ).append(", ");
    buf.append("created=").append( isCreated() ).append(", ");
    buf.append("deleted=").append( isDeleted() ).append(", ");
		buf.append("type=").append( type  ).append(", ");
    buf.append("session=").append( session );
    buf.append("]");
    return buf.toString();
  }



}
