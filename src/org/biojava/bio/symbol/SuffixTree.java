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


package org.biojava.bio.symbol;

import java.util.*;

import org.biojava.bio.seq.*;

/**
 * Suffix tree implementation.
 * <P>
 * The interface is a bit strange, as it needed to be as space-efficient as
 * possible. More work could be done on the space issue.
 *
 * @author Matthew Pocock
 */
public class SuffixTree {
  private FiniteAlphabet alphabet;
  private SuffixNode root;
  private List resList;
  private List counts;
  
  public FiniteAlphabet alphabet() {
    return alphabet;
  }

  public SuffixNode getRoot() {
    return root;
  }
  
  public SuffixNode getChild(SuffixNode node, Symbol r) {
    if(!alphabet().contains(r)) {
      return null;
    }
    int index = indexForRes(r);
    return getChild(node, index);
  }
  
  public SuffixNode getChild(SuffixNode node, int i) {
    if(!node.hasChild(i)) {
      node.addChild(this, i, new SimpleNode(alphabet.size()));
    }
    return node.getChild(i);
  }
  
  public void addSymbols(SymbolList rList, int window) {
    SuffixNode [] buf = new SuffixNode[window];
    int [] counts = new int[window];
    for(int i = 0; i < window; i++)
      buf[i] = getRoot();
    for(int p = 1; p <= rList.length(); p++) {
      Symbol r = rList.symbolAt(p);
      buf[p % window] = getRoot();
      for(int i = 0; i < window; i++) {
        int pi = (p + i) % window;
        if(buf[pi] != null) {
          buf[pi] = getChild(buf[pi], r);
          if(buf[pi] != null) {
            counts[i]++;
            buf[pi].setNumber(buf[pi].getNumber() + 1.0f);
          }
        }
      }
    }
    for(int i = 0; i < window; i++)
      incCounts(i+1, counts[i]);
      
  }
  
  protected void incCounts(int i, int c) {
    if(i < counts.size()) {
      Integer oldC = (Integer) counts.get(i-1);
      Integer newC = new Integer(oldC.intValue() + c);
      counts.set(i-1, newC);
    } else {
      counts.add(new Integer(c));
    }
  }
  
  public int maxLength() {
    return counts.size();
  }
  
  public int frequency(int length) {
    return ((Integer) counts.get(length - 1)).intValue();
  }
  
  public SuffixTree(FiniteAlphabet alphabet) {
    this.alphabet = alphabet;
    this.resList = alphabet.symbols().toList();
    this.counts = new ArrayList();
    this.root = new SimpleNode(alphabet.size());
  }
  
  public Symbol resForIndex(int i) {
    return (Symbol) resList.get(i);
  }

  public int indexForRes(Symbol r) {
    return resList.indexOf(r);
  }
  
  // A node in the suffix tree
  public static abstract class SuffixNode {
    abstract public boolean isTerminal();
    abstract public boolean hasChild(int i);
    abstract public float getNumber();
    abstract public void setNumber(float n);
    abstract SuffixNode getChild(int i);
    abstract void addChild(SuffixTree tree, int i, SuffixNode n);
  }

  private static class SimpleNode extends SuffixNode {
    private float number = 0.0f;
    private SuffixNode [] child;
    
    private SuffixNode [] childArray(SuffixTree tree) {
      if(child == null)
        child = new SuffixNode[tree.alphabet().size()];
      return child;
    }
    
    public boolean isTerminal() {
      return false;
    }
    
    public boolean hasChild(int i) {
      return child != null && child[i] != null;
    }
    
    public float getNumber() {
      return number;
    }
    
    SuffixNode getChild(int i) {
      if(hasChild(i))
        return child[i];
      return null;
    }
    
    void addChild(SuffixTree tree, int i, SuffixNode n) {
      childArray(tree)[i] = n;
    }
    
    public void setNumber(float n) {
      number = n;
    }
    
    SimpleNode(int c) {
      child = new SuffixNode[c];
    }
  }
}
