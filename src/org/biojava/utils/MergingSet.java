package org.biojava.utils;

import java.util.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class MergingSet
        extends AbstractSet
{
  private final Set sets;

  public MergingSet() {
    this.sets = new SmallSet();
  }

  public MergingSet(Set sets) {
    this.sets = new SmallSet(sets);
  }

  public void addSet(Set set) {
    sets.add(set);
  }

  public boolean removeSet(Set set) {
    return sets.remove(set);
  }

  public int size() {
    int size = 0;

    for(Iterator i = sets.iterator(); i.hasNext(); ) {
      Set s = (Set) i.next();
      size += s.size();
    }

    return size;
  }

  public boolean contains(Object o) {
    for (Iterator i = sets.iterator(); i.hasNext();) {
      Set s = (Set) i.next();
      if(s.contains(o)) {
        return true;
      }
    }

    return false;
  }

  public Iterator iterator() {
    return new MergingIterator(sets.iterator());
  }
}
