package org.biojava.bio;

/**
 * A constraint on the number of values a property can have.
 *
 * @author Matthew Pocock
 * @since 1.3
 */
public class CardinalityConstraint {
  public static final CardinalityConstraint ZERO
    = new CardinalityConstraint(0, 0);
  public static final CardinalityConstraint ZERO_OR_ONE
    = new CardinalityConstraint(0, 1);
  public static final CardinalityConstraint ANY
    = new CardinalityConstraint(0, Integer.MAX_VALUE);
  public static final CardinalityConstraint ONE
    = new CardinalityConstraint(1, 1);
  public static final CardinalityConstraint ONE_OR_MORE
    = new CardinalityConstraint(1, Integer.MAX_VALUE);
    
  private final int min;
  private final int max;
  
  public CardinalityConstraint(int min, int max) {
    this.min = min;
    this.max = max;
  }
  
  public int getMin() {
    return min;
  }
  
  public int getMax() {
    return max;
  }
  
  public boolean accept(int c) {
    return getMin() <= c && c <= getMax();
  }
  
  public int hashCode() {
    return min * max;
  }
  
  public boolean subConstraintOf(CardinalityConstraint subConstraint) {
    return
      getMin() <= subConstraint.getMin() &&
      getMax() >= subConstraint.getMax();
  }
  
  public boolean equals(Object o) {
    if(o == this) return true;
    
    if(o instanceof CardinalityConstraint) {
      CardinalityConstraint that = (CardinalityConstraint) o;
      return this.getMin() == that.getMin() && this.getMax() == that.getMax();
    }
    
    return false;
  }
}
