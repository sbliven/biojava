package org.biojava.bio.dp;

public interface TransitionListener {
  public void preCreateTransition(TransitionEvent te)
  throws ModelVetoException;
  
  public void postCreateTransition(TransitionEvent te);
  
  public void preDestroyTransition(TransitionEvent te)
  throws ModelVetoException;
  
  public void postDestroyTransition(TransitionEvent te);
  
  public void preChangeTransitionScore(TransitionEvent te)
  throws ModelVetoException;
  
  public void postChangeTransitionScore(TransitionEvent te);
}
