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

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.RepeatFeature;


/**
 * Provides access to RepeatFeatures in the datasource.
 */
public interface 
    RepeatFeatureAdaptor 
extends 
    FeatureAdaptor 
{
    RepeatFeature fetch( long internalID ) throws AdaptorException;
    
    /**
     * Warning: some repeats are stored unstranded (strand=0) which can produce unexpected results.
     * If location.strand = +1 or -1 then repeats where repeat.location.strand=0 will be 
     * omitted from the result. The 
     * solution is to set location.strand=0 and then filter out the those on the strand
     * you are not interested in.
     */
    List fetch( Location location ) throws  AdaptorException;

    long store( RepeatFeature repeat ) throws AdaptorException;

    void delete( long internalID )  throws AdaptorException;

    void delete( RepeatFeature repeat )  throws AdaptorException;

    /** 
     * Name of the default RepeatFeatureAdaptor available from a driver. 
     */
    final static String TYPE = "repeat_feature";
}//end RepeatFeatureAdaptor
