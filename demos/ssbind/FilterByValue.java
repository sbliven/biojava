package ssbind;

import org.biojava.bio.search.*;

/**
 * <p>
 * Filter complete sub hits by the numerical value of some property.
 * </p>
 *
 * <p>
 * The range that the property should be within is defined by the minVal and
 * maxVal properties. The property name to be checked is set by keyName. Any
 * sub hit that has this property and has a value that falls outside minVal and
 * maxVal will be silently dropped from the event stream, and not passed on to
 * the next handler in the chain.
 * </p>
 *
 * <h2>Example</h2>
 * <pre>
 * java ProcessBlast blast.out \
 *   "ssbind.FilterByValue(minVal=90 keyName=percentageIdentity)" \
 *    ssbind.Echoer
 * </pre>
 *
 * @author Matthew Pocock
 */
public class FilterByValue
extends SubHitFilter {
  private double minVal = Double.NEGATIVE_INFINITY;
  private double maxVal = Double.POSITIVE_INFINITY;
  private String keyName = "";
  
  public FilterByValue(SearchContentHandler delegate) {
    super(delegate);
  }
  
  public void setMinVal(double minVal) {
    this.minVal = minVal;
  }
  
  public double getMinVal() {
    return minVal;
  }
  
  public void setMaxVal(double maxVal) {
    this.maxVal = maxVal;
  }
  
  public double getMaxVal() {
    return maxVal;
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  public String getKeyName() {
    return keyName;
  }

  protected boolean accept(Object key, Object val) {
    if(keyName.equals(key)) {
      double score = (val instanceof Number)
        ? ((Number) val).doubleValue()
        : Double.parseDouble((String) val);
      return getMinVal() < score && score < getMaxVal();
    } else {
      return true;
    }
  }
}
