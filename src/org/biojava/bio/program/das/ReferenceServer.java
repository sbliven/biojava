package org.biojava.bio.program.das;

import java.net.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.db.*;

public class ReferenceServer
extends DataSource
implements Changeable {
  public static final ChangeType ANNOTATOR = new ChangeType(
    "Annotator added", ReferenceServer.class, "ANNOTATOR"
  );
  
  private Set annotators;
  private Set publicAnnotators;
  private transient ChangeSupport csup;
  
  {
    annotators = new HashSet();
    publicAnnotators = Collections.unmodifiableSet(annotators);
  }

  protected ChangeSupport getChangeSupport() {
    if(csup == null) {
      csup = new ChangeSupport();
    }
    return csup;
  }
  
  protected boolean hasListeners() {
    return csup != null;
  }
  
  public void addChangeListener(ChangeListener cl) {
    getChangeSupport().addChangeListener(cl);
  }
  
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    getChangeSupport().addChangeListener(cl, ct);
  }

  public void removeChangeListener(ChangeListener cl) {
    getChangeSupport().addChangeListener(cl);
  }

  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    getChangeSupport().addChangeListener(cl, ct);
  }

  ReferenceServer(URL dasURL, String name, String description) {
    super(dasURL, dasURL, name, description);
  }
  
  void addAnnotator(DataSource ann) throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport();
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(this, ANNOTATOR);
        cs.firePreChangeEvent(ce);
        annotators.add(ann);
        cs.firePostChangeEvent(ce);
      }
    } else {
      annotators.add(ann);
    }
  }
  
  public Set getAnnotaters() {
    return publicAnnotators;
  }
  
  public SequenceDB getDB() throws BioException {
    return new DASSequenceDB(getURL());
  }
}

