package org.biojava.bio.program.das;

import java.net.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.distributed.*;

public class ReferenceServer
  extends
    DataSource
  implements
    Changeable
{
  public static final ChangeType ANNOTATOR = new ChangeType(
    "Annotator added", ReferenceServer.class, "ANNOTATOR"
  );
  
  private Set annotators;
  private Set publicAnnotators;
  private transient ChangeSupport changeSupport = null;

  protected boolean hasListeners() {
    return changeSupport != null;
  }

  protected ChangeSupport getChangeSupport(ChangeType ct) {
    if(changeSupport != null) {
      return changeSupport;
    }
    
    synchronized(this) {
      if(changeSupport == null) {
        changeSupport = new ChangeSupport();
      }
    }
    
    return changeSupport;
  }

  public final void addChangeListener(ChangeListener cl) {
    addChangeListener(cl, ChangeType.UNKNOWN);
  }

  public final void addChangeListener(ChangeListener cl, ChangeType ct) {
    ChangeSupport cs = getChangeSupport(ct);
    cs.addChangeListener(cl, ct);
  }

  public final void removeChangeListener(ChangeListener cl) {
    removeChangeListener(cl, ChangeType.UNKNOWN);
  }

  public final void removeChangeListener(ChangeListener cl, ChangeType ct) {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(ct);
      cs.removeChangeListener(cl, ct);
    }
  }
  
  public final boolean isUnchanging(ChangeType ct) {
    ChangeSupport cs = getChangeSupport(ct);
    return cs.isUnchanging(ct);
  }
  
  {
    annotators = new HashSet();
    publicAnnotators = Collections.unmodifiableSet(annotators);
  }

  ReferenceServer(URL dasURL, String name, String description) {
    super(dasURL, dasURL, name, description);
  }
  
  void addAnnotator(DataSource ann) throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(ANNOTATOR);
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

    public DistDataSource getDistDataSource() 
        throws BioException
    {
	return new DASDistDataSource(getURL());
    }
}

