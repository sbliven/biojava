package org.biojava.bio.program.das;

import java.net.*;

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
}

