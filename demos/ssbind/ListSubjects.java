package ssbind;

import org.biojava.bio.search.*;

/**
 * <p>
 * Lists all subject sequence names in the report.
 * </p>
 *
 * <h2>Example</h2>
 * <pre>
 * java ProcessBlastReport blast.out ssbind.ListSubjects
 * </pre>
 *
 * @author Matthew Pocock
 */
public class ListSubjects
extends SearchContentAdapter {
  public void addHitProperty(Object key, Object val) {
    if("HitId".equals(key)) {
      System.out.println(val);
    }
  }
}
