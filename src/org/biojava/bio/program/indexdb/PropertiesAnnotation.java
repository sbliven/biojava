package org.biojava.bio.program.indexdb;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;

/**
 * <code>PropertiesAnnotation</code>.
 *
 * @author Original author unknown
 * @author Keith James
 */
class PropertiesAnnotation
extends AbstractAnnotation {
  private Properties props;
  private File propsFile;
  
  public PropertiesAnnotation(File propsFile) {
    this.propsFile = propsFile;
    this.props = new Properties();
    
    if(propsFile.exists()) {
      try {
        props.load(new FileInputStream(propsFile));
      } catch (IOException ioe) {
        throw new BioError(ioe, "Assertion Failure: could not load properties");
      }
    }
  }
  
  public void commit() {
    try {
      props.store(new FileOutputStream(propsFile), "Meta-Data");
    } catch (IOException ioe) {
      try {
        rollback();
      } catch (BioError be) {
        throw new BioError(be, "Catastrophic failure: could not roll back after failed commit");
      }
      throw new BioError("Could not commit");
    }
  }
  
  public void rollback() {
    if(propsFile.exists()) {
      try {
        props.load(new FileInputStream(propsFile));
      } catch (IOException ioe) {
        throw new BioError("Could not roll back");
      }
    } else {
      props.clear();
    }
  }
  
  protected Map getProperties() {
    return props;
  }
  
  protected boolean propertiesAllocated() {
    return true;
  }
}
