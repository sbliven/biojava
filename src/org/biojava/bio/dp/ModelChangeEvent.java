package org.biojava.bio.dp;

import java.util.EventObject;
import java.io.Serializable;

public class ModelChangeEvent extends EventObject {
  public MarkovModel getSourceModel() {
    return (MarkovModel) getSource();
  }
  
  public ModelChangeEvent(MarkovModel sourceModel) {
    super(sourceModel);
  }
}
