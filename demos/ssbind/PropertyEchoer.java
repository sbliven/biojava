package ssbind;

import org.biojava.bio.search.*;

/**
 * <p>
 * Print out the value of a single property.
 * </p>
 *
 * <h2>Example</h2>
 * <pre>
 * java ProcessBlastReport out.blast "PropertyEchoer(keyName=numberOfIdentities)"
 * </pre>
 *
 * @author Matthew Pocock
 */
public class PropertyEchoer
extends SearchContentAdapter {
  private String keyName;

  public PropertyEchoer() {
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  public String getKeyName() {
    return keyName;
  }

  public void addSearchProperty(Object key, Object val) {
    if(keyName.equals(key)) {
      System.out.println(val);
    }
  }

  public void addHitProperty(Object key, Object val) {
    if(keyName.equals(key)) {
      System.out.println(val);
    }
  }
  public void addSubHitProperty(Object key, Object val) {
    if(keyName.equals(key)) {
      System.out.println(val);
    }
  }
}