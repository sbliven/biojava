package org.biojava.bio.dp;

public class ModelVetoException extends Exception {
  private final ModelChangeEvent mce;
  
  public ModelChangeEvent getModelChangeEvent() {
    return mce;
  }
  
  public ModelVetoException(String message, ModelChangeEvent mce) {
    super(message);
    this.mce = mce;
  }
}
