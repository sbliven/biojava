package org.biojava.bio.dp;

public class TransitionEvent extends ModelChangeEvent {
  private final State start;
  private final State end;
  
  private final double oldScore;
  private final double newScore;
  
  public State getStart() {
    return start;
  }
  
  public State getEnd() {
    return end;
  }
  
  public double getOldScore() {
    return oldScore;
  }
  
  public double getNewScore() {
    return newScore;
  }
  
  public TransitionEvent(MarkovModel sourceModel, State start, State end) {
    this(sourceModel, start, end, Double.NaN, Double.NaN);
  }
  
  public TransitionEvent(
    MarkovModel sourceModel,
    State start, State end,
    double oldScore, double newScore
  ) {
    super(sourceModel);
    this.start = start;
    this.end = end;
    this.oldScore = oldScore;
    this.newScore = newScore;
  }  
}
