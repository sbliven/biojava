package org.biojava.bio.dp;

import java.util.EventObject;

public class ModelChangeEvent extends EventObject {
  public MarkovModel getSourceModel() {
    return (MarkovModel) getSource();
  }
  
  public ModelChangeEvent(MarkovModel sourceModel) {
    super(sourceModel);
  }
}
