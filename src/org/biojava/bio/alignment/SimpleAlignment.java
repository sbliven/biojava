/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */


package org.biojava.bio.alignment;

import java.util.*;

import org.biojava.bio.seq.*;

/**
 * A simple implementation of an Alignment.
 */
public class SimpleAlignment implements Alignment {
  private int length = -1;
  private Set sequenceSet;
  private Annotation annotation = null;

  public int length() {
    return length;
  }
  
  public Set getSequences() {
    return sequenceSet;
  }
  
  public Residue getResidue(ResidueList seq, int column) {
    return seq.residueAt(column);
  }
  
  public Map getColumn(int column) {
    return new ColumnMap(column);
  }
  
  public Alignment subAlignment(Set sequences, Location loc) {
    Set seqs = new HashSet();
    for(Iterator i = sequences.iterator(); i.hasNext(); ) {
      ResidueList seq = (ResidueList) i.next();
      seqs.add(loc.residues(seq));
    }
    return new SimpleAlignment(seqs);
  }
  
  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }
  
  public SimpleAlignment(Set sequences) {
    this.sequenceSet = sequences;
    Iterator i = sequences.iterator();
    if(i.hasNext()) {
      this.length = ((ResidueList) i.next()).length();
    }
  }

  protected class ColumnMap extends AbstractMap {
    private int column;
    
    public int size() {
      return getSequences().size();
    }
    public boolean isEmpty() {
      return getSequences().isEmpty();
    }
    public boolean containsKey(Object key) {
      return getSequences().contains(key);
    }
    public boolean containsValue(Object value) {
      return values().contains(value);
    }
    public Object get(Object key) {
      return getResidue((ResidueList) key, column);
    }
    public Set keySet() {
      return getSequences();
    }
    public Set entrySet() {
      return new AbstractSet() {
        public int size() {
          return getSequences().size();
        }
        public Iterator iterator() {
          return new Iterator() {
            private Iterator ki = getSequences().iterator();
            public boolean hasNext() {
              return ki.hasNext();
            }
            public Object next() {
              final Object key = ki.next();
              return new Map.Entry() {
                public Object getKey() {
                  return key;
                }
                public Object getValue() {
                  return get(key);
                }
                public Object setValue(Object val) {
                  throw new UnsupportedOperationException();
                }
                public boolean equals(Object o) {
                  Map.Entry me = (Map.Entry) o;
                  return me.getKey().equals(getKey()) &&
                         me.getValue().equals(getValue());
                }
                public int hashCode() {
                  return getKey().hashCode() ^ getValue().hashCode();
                }
              };
            }
            public void remove() {
              throw new UnsupportedOperationException();
            }
          };
        }
      };
    }
    public ColumnMap(int column) {
      this.column = column;
    }
  }
}
