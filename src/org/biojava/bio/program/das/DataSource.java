/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package org.biojava.bio.program.das;

import java.net.*;
import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;
import org.biojava.utils.*;
import org.biojava.bio.seq.distributed.*;

/**
 * Reflect a datasource (reference or annotation) in the DAS1 network.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public class DataSource {
  private URL dasURL;
  private URL mapURL;
  private String name;
  private String description;
  
  DataSource(URL dasURL, URL mapURL, String name, String description) {
    this.dasURL = dasURL;
    this.mapURL = mapURL;
    this.name = name;
    this.description = description;
  }

  public URL getURL() {
    return dasURL;
  }
  
  public URL getMapURL() {
    return mapURL;
  }
  
  public String getName() {
    return name;
  }
  
    void setName(String name) {
	this.name = name;
    }

    public String getDescription() {
	return description;
    }

    void setDescription(String description) {
	this.description = description;
    }
  
    public int hashCode() {
	return getURL().hashCode();
    }

    public boolean equals(Object other) {
	if(! (other instanceof DataSource) ) {
	    return false;
	}
    
	DataSource od = (DataSource) other;
	return od.getURL().equals(this.getURL());
    }

    public DistDataSource getDistDataSource() 
        throws BioException
    {
	return new DASAnnotationDistDataSource(getURL());
    }
}

