package org.biojava.bio.program.indexdb;

import java.util.List;
import java.util.Comparator;
import org.biojava.utils.Commitable;

interface SearchableList
extends
  List,
  Commitable
{
  public Object search(String id);
  
  public List searchAll(String id);
  
  public Comparator getComparator();
}
