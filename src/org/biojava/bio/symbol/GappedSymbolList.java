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
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * This implementation of SymbolList wrappes another one, allowing you to insert gaps.
 * <P>
 * You could make a SymbolList that contains gaps directly. However, this
 * leaves you with a nasty problem if you wish to support gap-edit opperations. Also, the orriginal
 * SymbolList must either be coppied or lost.
 * <P>
 * GappedSymbolList solves these problems. It wraps up a source sequence, and views it through
 * a data-structure that places gaps. You can add and remove the gaps by using the public API.
 * <P>
 * For gap-insert operations, the insert index is the position that will become a gap. The
 * symbol currently there will move to a higher index. To insert leading gaps, add gaps at index
 * 1. To insert trailing gaps, add gaps at index length+1.
 *
 * @author Matthew Pocock
 */
public class GappedSymbolList
extends AbstractSymbolList implements Serializable {
  /**
   * The Alphabet - the same as source but guaranteed to include the gap character.
   */
  private final Alphabet alpha;
  
  /**
   * The SymbolList to view.
   */
  private final SymbolList source;
  
  /**
   * The list of ungapped blocks that align between source and this view.
   */
  private final ArrayList blocks;
  
  /**
   * The total length of the alignment - necisary to allow leading & trailing gaps.
   */
  private int length;
  
  /**
   * Finds the index of the block containing the source coordinate indx.
   *
   * @param indx  the index to find
   * @return the index of the Block containing indx
   */
  protected final int findSourceBlock(int indx) {
    int i = blocks.size() / 2;
    int imin = 0;
    int imax = blocks.size() - 1;
    
    do {
      Block b = (Block) blocks.get(i);
      if(b.sourceStart <= indx && b.sourceEnd >= indx) {
        return i;
      } else {
        if(b.sourceStart < indx) {
          imin = i+1;
          i = imin + (imax - imin) / 2;
        } else {
          imax = i-1;
          i = imin + (imax - imin) / 2;
        }
      }
    } while(imin <= imax);
    
    throw new BioError(
      "Something is screwed. Could not find source block containing index " +
      indx + " in sequence of length " + source.length()
    );
  }

  /**
   * Finds the index of the Block containing indx within the view ranges.
   * <P>
   * If indx is not within a view block, then it is the index of a gap. The method will
   * return -1.
   *
   * @param indx  the index to find within a view range.
   * @return  the index of the block containing index, or -1 if no block contains it
   */
  protected final int findViewBlock(int indx) {
    int i = blocks.size() / 2;
    int imin = 0;
    int imax = blocks.size() - 1;
    //System.out.println("Searching for " + indx);
    do {
      //System.out.println(imin + " < " + i + " < " + imax);
      Block b = (Block) blocks.get(i);
      //System.out.println("Checking " + b.viewStart + ".." + b.viewEnd);
      if(b.viewStart <= indx && b.viewEnd >= indx) {
        //System.out.println("hit");
        return i;
      } else {
        if(b.viewStart < indx) {
          //System.out.println("Too low");
          imin = i+1;
          i = imin + (imax - imin) / 2;
        } else {
          //System.out.println("Too high");
          imax = i-1;
          i = imin + (imax - imin) / 2;
        }
      }
    } while(imin <= imax);
    
    return -1;
  }

  /**
   * Finds the index of the Block before the gap at indx within the following gap.
   *
   * @param indx  the index to find within a gap
   * @return  the index of the block with indx in the gap
   */
  protected final int findSourceGap(int indx) {
    int i = blocks.size() / 2;
    int imin = 0;
    int imax = blocks.size() - 1;
    
    do {
      Block b = (Block) blocks.get(i);
      if(b.sourceStart <= indx && b.sourceEnd >= indx) {
        return -1;
      } else {
        if(b.sourceStart < indx) {
          imin = i+1;
          i = imin + (imax - imin) / 2;
        } else {
          imax = i-1;
          i = imin + (imax - imin) / 2;
        }
      }
    } while(imin <= imax);
    
    Block b = (Block) blocks.get(i);
    if(b.viewEnd < indx) {
      return i;
    } else {
      return i-1;
    }
  }
  
  /**
   * Finds the index of the Block before the gap at indx within the view range.
   * <P>
   * If indx is in-fact a real symbol, then there will be no Block before it. In this
   * case, the method returns -2. It returns -1 if indx is within the leading gaps and
   * blocks.size()-1 if it is within the trailing gaps.
   *
   * @param indx  the index to find within a view range
   * @return  the index of the block with indx in the following gap
   */
  protected final int findViewGap(int indx) {
    int i = blocks.size() / 2;
    int imin = 0;
    int imax = blocks.size() - 1;
    
    do {
      Block b = (Block) blocks.get(i);
      if(b.viewStart <= indx && b.viewEnd >= indx) {
        return -2;
      } else {
        if(b.viewStart < indx) {
          imin = i+1;
          i = imin + (imax - imin) / 2;
        } else {
          imax = i-1;
          i = imin + (imax - imin) / 2;
        }
      }
    } while(imin <= imax);

    if(i < blocks.size()) {    
      Block b = (Block) blocks.get(i);
      if(b.viewEnd < indx) {
        return i;
      } else {
        return i-1;
      }
    } else {
      return i-1;
    }
  }

  /**
   * Coordinate conversion from view to source.
   *
   * @param b the block containing indx
   * @param   indx the index to project
   * @return the position of indx projected from view to source
   */
  protected final int viewToSource(Block b, int indx) {
    return indx - b.viewStart + b.sourceStart;
  }
  
  /**
   * Coordinate conversion from view to source.
   *
   * @param   indx the index to project
   * @return the position of indx projected from view to source
   * @throws IndexOutOfBoundsException if indx is not a valid view index
   */
  public final int viewToSource(int indx)
  throws IndexOutOfBoundsException {
    if(indx < 1 || indx > length()) {
      throw new IndexOutOfBoundsException(
        "Attempted to address sequence (1.." + length() + ") at " + indx
      );
    }
    int j = findViewBlock(indx);
    if(j == -1) {
      return -1;
    } else {
      return viewToSource(
        (Block) blocks.get(j),
        indx
      );
    }
  }
  
  /**
   * Coordinate conversion from source to view.
   *
   * @param b the block containing indx
   * @param   indx the index to project
   * @return the position of indx projected from source to view
   */
  protected final int sourceToView(Block b, int indx) {
    return indx - b.sourceStart + b.viewStart;
  }
  
  /**
   * Coordinate conversion from source to view.
   *
   * @param   indx the index to project
   * @return the position of indx projected from source to view
   * @throws IndexOutOfBoundsException if indx is not a valid source index
   */
  public final int sourceToView(int indx)
  throws IndexOutOfBoundsException {
    if(indx < 1 || indx > source.length()) {
      throw new IndexOutOfBoundsException(
        "Attempted to address source sequence (1.." + length() + ") at " + indx
      );
    }
    int j = findSourceBlock(indx);
    return sourceToView(
      (Block) blocks.get(j),
      indx
    );
  }
  
  /**
   * Renumber the view indexes from block, adding delta to each offset.
   * <P>
   * This adjusts viewStart and viewEnd to be += delta for each block i->blocks.size(), 
   * and sets the total length to += delta.
   *
   * @param i the first 
   */
  protected final void renumber(int i, int delta) {
    for(int j = i; j < blocks.size(); j++) {
      Block b = (Block) blocks.get(j);
      b.viewStart += delta;
      b.viewEnd += delta;
    }
    length += delta;
  }
  
  /**
   * Add a single gap at pos within the view coordintates.
   * <P>
   * this.symbolAt(pos) will then return gap. Adding a gap at 1 will prepend gaps. Adding
   * a gap at (length+1) will append a gap.
   *
   * @param pos  the position to add a gap before
   * @throws IndexOutOfBoundsException if pos is not within 1->length+1
   */
  public void addGapInView(int pos)
  throws IndexOutOfBoundsException {
    addGapsInView(pos, 1);
  }

  /**
   * Add length gaps at pos within the view coordinates.
   * <P>
   * this.symbolAt(i) will then return gap for i = (pos .. pos+count-1).
   * Adding gaps at 1 will prepend gaps. Adding
   * gaps at (length+1) will append gaps.
   *
   * @param pos  the position to add a gap before
   * @param length  the number of gaps to insert
   * @throws IndexOutOfBoundsException if pos is not within 1->length+1
   */
  public void addGapsInView(int pos, int length)
  throws IndexOutOfBoundsException {
    if(pos < 1 || pos > (length() + 1)) {
      throw new IndexOutOfBoundsException(
        "Attempted to add a gap outside of this sequence (1.." + length() + ") at " + pos
      );
    }
    
    int i = blocks.size() / 2;
    int imin = 0;
    int imax = blocks.size() - 1;
    
    do {
      Block b = (Block) blocks.get(i);
      if(b.viewStart < pos && b.viewEnd >= pos) { // found block that need splitting with gaps
        Block c = new Block(viewToSource(b, pos), b.sourceEnd, pos, b.viewEnd);
        b.viewEnd = pos-1;
        b.sourceEnd = viewToSource(b, pos-1);
        blocks.add(i+1, c);
        renumber(i+1, length);
        return;
      } else {
        if(b.viewStart < pos) {
          imin = i+1;
          i = imin + (imax - imin) / 2;
        } else {
          imax = i-1;
          i = imin + (imax - imin) / 2;
        }
      }
    } while(imin <= imax);
    
    // extending an already existing run of gaps;
    if(i < blocks.size()) {
      Block b = (Block) blocks.get(i);
      if(b.viewStart <= pos) {
        i--;
      }
    } else {
      i--;
    }
    renumber(i+1, length);
  }
  
  /**
   * Add a gap at pos within the source coordinates.
   *
   * @param pos where to add the gap
   * @throws IndexOutOfBoundsException if pos is not within 1->source.length()
   */
  public void addGapInSource(int pos)
  throws IndexOutOfBoundsException {
    addGapsInSource(pos, 1);
  }
  
  /**
   * Add length gaps at pos within the source coordinates.
   *
   * @param pos where to add the gap
   * @param length  how many gaps to add
   * @throws IndexOutOfBoundsException if pos is not within 1->source.length()
   */
  public void addGapsInSource(int pos, int length) {
    if(pos < 1 || pos > (length() + 1)) {
      throw new IndexOutOfBoundsException(
        "Attempted to add a gap outside of this sequence (1.." + length() + ") at " + pos
      );
    }
    
    int i = blocks.size() / 2;
    int imin = 0;
    int imax = blocks.size() - 1;
    
    do {
      Block b = (Block) blocks.get(i);
      if(b.sourceStart < pos && b.sourceEnd >= pos) { // found block that need splitting with gaps
        Block c = new Block(pos, b.sourceEnd, sourceToView(b, pos), b.viewEnd);
        b.viewEnd = sourceToView(b, pos-1);
        b.sourceEnd = pos-1;
        blocks.add(i+1, c);
        renumber(i+1, length);
        return;
      } else {
        if(b.sourceStart < pos) {
          imin = i+1;
          i = imin + (imax - imin) / 2;
        } else {
          imax = i-1;
          i = imin + (imax - imin) / 2;
        }
      }
    } while(imin <= imax);
    
    // extending an already existing run of gaps;
    if(i < blocks.size()) {
      Block b = (Block) blocks.get(i);
      if(b.sourceStart <= pos) {
        i--;
      }
    }
    renumber(i+1, length);
  }
  
  /**
   * Remove a single gap at position pos in this GappedSymbolList.
   *
   * @param pos where to remove the gap
   * @throws IndexOutOfBoundsException if pos is not within 1..length
   * @throws IllegalSymbolException if the symbol at pos is not a gap
   */
  public void removeGap(int pos)
  throws IndexOutOfBoundsException, IllegalSymbolException { 
    if(pos < 1 || pos > length()) {
      throw new IndexOutOfBoundsException(
        "Attempted to remove gap outside of this sequence (1.." + length() + ") at " + pos
      );
    }
    int i = findViewGap(pos);
    if(i == -2) {
      throw new IllegalSymbolException(
        "Attempted to remove a gap at a non-gap index: " + pos + " -> " + symbolAt(pos).getName()
      );
    }
    
    if(i == -1 || i == (blocks.size()-1)) { // at the beginning or the end
      renumber(i+1, -1);
    } else { // internal
      Block l = (Block) blocks.get(i);
      Block r = (Block) blocks.get(i+1);
      renumber(i+1, -1);
      int gap = r.viewStart - l.viewEnd;
      if(gap == 1) { // removing the last gap - need to join blocks l & r together
        l.sourceEnd = r.sourceEnd;
        l.viewEnd = r.viewEnd;
        blocks.remove(i+1);
      }
    }
  }
  
  /**
   * Remove some gaps at position pos in this GappedSymbolList.
   *
   * @param pos where to remove the gaps
   * @param length how many to remove
   * @throws IndexOutOfBoundsException if pos is not within 1..length() or if
   *         some of the Symbols within pos->(pos+length-1) are not gap Symbols
   * @throws IllegalSymbolException if the symbol at pos is not a gap
   */
  public void removeGaps(int pos, int length)
  throws IndexOutOfBoundsException, IllegalSymbolException {
    int end = pos + length - 1;
    if(
      pos < 1 ||
      length < 1 ||
      end > length()
    ) {
      throw new IndexOutOfBoundsException(
        "Attempted to remove gap outside of this sequence (1.." + length() + ") at (" +
        pos + ".." + end + ")"
      );
    }
    int i = findViewGap(pos);
    if(i == -2) {
      throw new IllegalSymbolException(
        "Attempted to remove a gap at a non-gap index: " + pos + " -> " + symbolAt(pos).getName()
      );
    }
    
    if(i == -1) { // removing track at the beginning
      Block b = (Block) blocks.get(0);
      if(b.viewStart <= end) {
        throw new IllegalSymbolException(
          "Attempted to remove some non-gap characters at (" + pos + ".." + end + ")"
        );
      }
      renumber(i+1, -length);
    } else if(i == (blocks.size()-1)) { // removing trailing gaps
      this.length -= length;
    } else { // removing internal gaps
      Block l = (Block) blocks.get(i);
      Block r = (Block) blocks.get(i+1);
      renumber(i+1, -length);
      int gap = r.viewStart - l.viewEnd;
      if(gap < length) {
        throw new IllegalSymbolException(
          "Attempted to remove some non-gap characters at (" + pos + ".." + end + ")"
        );
      } else if( gap == length) { // deleted an entire gapped region
        l.sourceEnd = r.sourceEnd;
        l.viewEnd = r.viewEnd;
        blocks.remove(i+1);
      }
    }
  }
  
  public Alphabet getAlphabet() {
    return alpha;
  }
  
  public int length() {
    return length;
  }
  
  public Symbol symbolAt(int indx)
  throws IndexOutOfBoundsException {
    if(indx > length() || indx < 1) {
      throw new IndexOutOfBoundsException(
        "Attempted to read outside of this sequence (1.." + length() + ") at " + indx
      );
    }
    int i = findViewBlock(indx);
    if(i == -1) {
      return AlphabetManager.getGapSymbol();
    } else {
      Block b = (Block) blocks.get(i);
      return source.symbolAt(b.sourceStart - b.viewStart + indx);
    }
  }

  /**
   * Return the index of the first Symbol that is not a Gap character.
   * <P>
   * All symbols before firstNonGap are leading gaps. firstNonGap is effectively
   * the index in the view of symbol 1 in the original sequence.
   *
   * @return the index of the first character not to be a gap
   */
  public int firstNonGap() {
    int first = ((Block) blocks.get(0)).viewStart;
    return first;
  }
  
  /**
   * Return the index of the last Symbol that is not a Gap character.
   * <P>
   * All symbols after lastNonGap untill length are trailing gaps.
   * lastNonGap is effectively the index in the view of symbol length
   * in the original sequence.
   *
   * @return the index of the last character not to be a gap
   */
  public int lastNonGap() {
    int last = ((Block) blocks.get(blocks.size() - 1)).viewEnd;
    return last;
  }
  
  /**
   * Create a new GappedSymbolList that will view source.
   *
   * @param source  the underlying sequence
   */
  public GappedSymbolList(SymbolList source) {
    this.source = source;
    this.alpha = source.getAlphabet();
    this.blocks = new ArrayList();
    this.length = source.length();
    Block b = new Block(1, length, 1, length);
    blocks.add(b);
  }

  public void dumpBlocks() {
    for(Iterator i = blocks.iterator(); i.hasNext(); ) {
      Block b = (Block) i.next();
      System.out.println(b.sourceStart + ".." + b.sourceEnd + "\t" + b.viewStart + ".." + b.viewEnd);
    }
  }
  
  /**
   * An aligned block.
   * <P>
   * The alignment is actualy stoored as a list of these objects. Each block is contiguous
   * with the next in the source fields, but may be gapped in the view fields.
   *
   * @author Matthew Pocock
   */
  private final class Block implements Serializable {
    public int sourceStart, sourceEnd;
    public int viewStart, viewEnd;
    
    public Block(int sourceStart, int sourceEnd, int viewStart, int viewEnd) {
      this.sourceStart = sourceStart;
      this.sourceEnd = sourceEnd;
      this.viewStart = viewStart;
      this.viewEnd = viewEnd;
    }
  }
}
