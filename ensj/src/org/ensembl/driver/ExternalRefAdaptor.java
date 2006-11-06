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

import org.ensembl.datamodel.ExternalRef;

/**
 * Provides access to ExternalRefs in the datasource.
 */
public interface ExternalRefAdaptor extends Adaptor {

  
  /**
   * Fetches ExternalRefs with the specified _internalID_.
   * @param internalID id of the persistent object to find references for
   * 
   * @return List containing zero or more ExternalRefs.
   */
  
  List fetch(long internalID) throws AdaptorException;

  /**
   * Fetches ExternalRefs for the item with the specified _type_ and _internalID_.
   * @param internalID id of the persistent object to find references for
   * @param type type of object, must be one of ExternalRef.GENE, ExternalRef.TRANSLATION, ExternalRef.TRANSCRIPT or ExternalRef.CLONE_FRAGMENT
   * 
   * @return List containing zero or more ExternalRefs for the specified item.
   */
  List fetch(long internalID, int type) throws AdaptorException;


  /**
   * Fetches ExternalRefs where name appears as either a primaryID, displayID
   * or synonym.
   * @param name Synonym to find matching refs for.
   * 
   * @return xref with synonyms set to a list containing zero or more ExternalRefs with the specified synonym. 
   */
  List fetch(String name) throws AdaptorException;

  /**
   * Fetches synonyms for specied xref and sets them on the xref.
   * 
   * @param xRef external ref to set the synonyms on.
   * @return xRef
   * @throws AdaptorException
   */
  ExternalRef  fetchCompleteSynonyms(ExternalRef xRef) throws AdaptorException;
  
  /**
   * Fetchs the goLinkageType for the xref and sets it on the xref.
   * 
   * @param xRef external ref to set goLinkageType on.
   * @return xRef with go linkage type set if available otherwise it will be null.
   * @throws AdaptorException
   */
  ExternalRef fetchCompleteGoLinkageType(ExternalRef xRef) throws AdaptorException;
  
  
  /**
   * Fetchs the identity query and target for the xref and sets them on the xref.
   * 
   * @param xRef external ref to set identity info for.
   * @return xRef with identity info set if available otherwise it will be null.
   * @throws AdaptorException
   */
  ExternalRef fetchCompleteIdentity(ExternalRef xRef) throws AdaptorException;
  
  
  /**
   * Stores ExternalRef.
   * @param externalRef ExternalRef to be stored.
   * @return internalID assigned to ExternalRef.
   */
  long store(ExternalRef externalRef) throws AdaptorException;
  
  /**
    * Stores a link between an ensembl object and an externalRef. 
    * @param ensemblInternalID internalID of the object to link.
    * @param ensemblType type of object to link.
    * @param externalRefInternalID internalID of the externalRef to link.
    * @return internalID of the link.
    * @throws AdaptorException
    */
   long storeObjectExternalRefLink(long ensemblInternalID, int ensemblType, long externalRefInternalID) throws AdaptorException;


  /**
   * Deletes ExternalRef, with specified internalID, from datasource, does
   * nothing if externalRef is not in datasource.
   * @param internalID internalID of externalRef to be deleted
   */
  void delete( long internalID ) throws AdaptorException;

  /**
   * Deletes externalRef from datasource, does nothing if externalRef is not
   * in datasource.
   * @param externalRef externalRef to be deleted
   */
  void delete( ExternalRef externalRef ) throws AdaptorException;

 /** 
   * Name of the default ExternalEefAdaptor available from a driver. 
   */
  final static String TYPE = "external_ref";
}
