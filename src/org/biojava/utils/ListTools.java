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
package org.biojava.utils;

import java.util.*;
import java.io.*;

public class ListTools implements Serializable{
  public static List createList(List l) {
    switch (l.size()) {
      case 0:
        return Collections.EMPTY_LIST;
      case 1:
        return Collections.nCopies(1, l.get(0));
      case 2:
        Doublet d = new Doublet(l.get(0), l.get(1));
        return d;
      case 3:
        Triplet t = new Triplet(l.get(0), l.get(1), l.get(2));
        return t;
      default:
        return new ArrayList(l);
    }
  }

  public static List createList(Object[] a) {
    switch (a.length) {
      case 0:
        return Collections.EMPTY_LIST;
      case 1:
        return Collections.nCopies(1, a[0]);
      case 2:
        Doublet d = new Doublet(a[0],a[1]);
        return d;
      case 3:
        Triplet t = new Triplet(a[0],a[1],a[2]);
        return t;
      default:
        return Arrays.asList(a);
    }
  }

  public static class Doublet extends AbstractList implements Serializable {
    private Object a;
    private Object b;

    public Doublet() {}
    public Doublet(Object a, Object b) {
      this();
      set(a, b);
    }

    public void set(Object a, Object b) {
      this.a = a;
      this.b = b;
    }

    public void setA(Object a) {
      this.a = a;
    }

    public void setB(Object b) {
      this.b = b;
    }

    public Object getA() {
      return a;
    }

    public Object getB() {
      return b;
    }

    public int size() {
      return 2;
    }

    public Object get(int indx) {
      switch (indx) {
        case 0:
          return a;
        case 1:
          return b;
        default:
          throw new IndexOutOfBoundsException("indx must be 0 or 1");
      }
    }

    public Iterator getIterator() {
      return new Iterator() {
        private int indx = 0;

        public boolean hasNext() {
          return indx < 2;
        }

        public Object next() {
          return get(indx++);
        }

        public void remove()
        throws UnsupportedOperationException {
          throw new UnsupportedOperationException();
        }
      };
    }

    public int hashCode() {
      int hashcode = 1;
      hashcode = 31*hashcode + a.hashCode();
      hashcode = 31*hashcode + b.hashCode();
      return hashcode;
    }

    public boolean equals(Object o) {
      if(! (o instanceof List) ) {
        return false;
      }

      List other = (List) o;
      if(other.size() != 2) {
        return false;
      }

      return other.get(0).equals(a) && other.get(1).equals(b);
    }
  }

  public static class Triplet extends AbstractList implements Serializable {
    private Object a;
    private Object b;
    private Object c;

    public Triplet() {}
    public Triplet(Object a, Object b, Object c) {
      this();
      set(a, b, c);
    }

    public void set(Object a, Object b, Object c) {
      this.a = a;
      this.b = b;
      this.c = c;
    }

    public void setA(Object a) {
      this.a = a;
    }

    public void setB(Object b) {
      this.b = b;
    }

    public void setC(Object c) {
      this.c = c;
    }

    public Object getA() {
      return a;
    }

    public Object getB() {
      return b;
    }

    public Object getC() {
      return c;
    }

    public int size() {
      return 3;
    }

    public Object get(int indx) {
      switch (indx) {
        case 0:
          return a;
        case 1:
          return b;
        case 2:
          return c;
        default:
          throw new IndexOutOfBoundsException("indx must be 0 or 1");
      }
    }

    public Iterator getIterator() {
      return new Iterator() {
        private int indx = 0;

        public boolean hasNext() {
          return indx < 3;
        }

        public Object next() {
          return get(indx++);
        }

        public void remove()
        throws UnsupportedOperationException {
          throw new UnsupportedOperationException();
        }
      };
    }

    public int hashCode() {
      int hashcode = 1;
      hashcode = 31*hashcode + a.hashCode();
      hashcode = 31*hashcode + b.hashCode();
      hashcode = 31*hashcode + c.hashCode();
      return hashcode;
    }

    public boolean equals(Object o) {
      if(! (o instanceof List) ) {
        return false;
      }

      List other = (List) o;
      if(other.size() != 3) {
        return false;
      }

      return other.get(0).equals(a) && other.get(1).equals(b) && other.get(2).equals(c);
    }
  }
}
