package org.biojava.bio.ontology;

import org.biojava.bio.BioException;

public class OntologyException
extends BioException {
  public OntologyException() {
    super();
  }
  
  public OntologyException(String message) {
    super(message);
  }
  
  public OntologyException(Throwable cause) {
    super(cause);
  }
  
  public OntologyException(Throwable cause, String message) {
    super(cause, message);
  }
}
