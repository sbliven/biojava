package ssbind;

import org.biojava.bio.search.*;

public class ListSubjects
extends SearchContentAdapter {
  public void addHitProperty(Object key, Object val) {
    if("HitId".equals(key)) {
      System.out.println(val);
    }
  }
}
