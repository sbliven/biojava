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


package org.biojava.bio.seq.tools;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * This implementation of ResidueList wrappes another one, allowing you to insert gaps.
 */
public class GappedResidueList extends AbstractResidueList {
  private final Alphabet alpha;
  private final ResidueList source;
  private final ArrayList blocks;
  private int length;
  
  /**
   * Finds the block containing the source coordinate indx.
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
   * Finds the index of the Block containing indx within the view range or -1.
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
   * Finds the index of the Block before the gap at indx within the source range or -1.
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
   * Finds the index of the Block before the gap at indx within the view range or -2.
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

  protected final int viewToSource(Block b, int indx) {
    return indx - b.viewStart + b.sourceStart;
  }
  
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
  
  protected final int sourceToView(Block b, int indx) {
    return indx - b.sourceStart + b.viewStart;
  }
  
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
   * this.residueAt(pos) will then return gap. Adding a gap at 1 will prepend gaps. Adding
   * a gap at (length+1) will append a gap.
   */
  public void addGapInView(int pos)
  throws IndexOutOfBoundsException {
    addGapsInView(pos, 1);
  }

  /**
   * Add length gaps at pos within the view coordinates.
   * <P>
   * this.residueAt(i) will then return gap for i = (pos .. pos+count-1).
   * Adding gaps at 1 will prepend gaps. Adding
   * gaps at (length+1) will append gaps.
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
   * Add length gaps at pos within the source coordinates.
   */
  public void addGapInSource(int pos)
  throws IndexOutOfBoundsException {
    addGapsInSource(pos, 1);
  }
  
  /**
   * Add length gaps at pos within the source coordinates.
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
   * Remove a single gap at position pos in this GappedResidueList.
   *
   * @throws IndexOutOfBoundsException if pos is not within 1..length
   * @throws IllegalResidueException if the residue at pos is not a gap
3   */
  public void removeGap(int pos)
  throws IndexOutOfBoundsException, IllegalResidueException { 
    if(pos < 1 || pos > length()) {
      throw new IndexOutOfBoundsException(
        "Attempted to remove gap outside of this sequence (1.." + length() + ") at " + pos
      );
    }
    int i = findViewGap(pos);
    if(i == -2) {
      throw new IllegalResidueException(
        "Attempted to remove a gap at a non-gap index: " + pos + " -> " + residueAt(pos).getName()
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
  
  public void removeGaps(int pos, int length)
  throws IndexOutOfBoundsException, IllegalResidueException {
    int end = pos + length;
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
      throw new IllegalResidueException(
        "Attempted to remove a gap at a non-gap index: " + pos + " -> " + residueAt(pos).getName()
      );
    }
    
    if(i == -1) { // removing track at the beginning
      Block b = (Block) blocks.get(0);
      if(b.viewStart <= end) {
        throw new IllegalResidueException(
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
        throw new IllegalResidueException(
          "Attempted to remove some non-gap characters at (" + pos + ".." + end + ")"
        );
      } else if( gap == length) { // deleted an entire gapped region
        l.sourceEnd = r.sourceEnd;
        l.viewEnd = r.viewEnd;
        blocks.remove(i+1);
      }
    }
  }
  
  public Alphabet alphabet() {
    return alpha;
  }
  
  public int length() {
    return length;
  }
  
  public Residue residueAt(int indx)
  throws IndexOutOfBoundsException {
    if(indx > length() || indx < 1) {
      throw new IndexOutOfBoundsException(
        "Attempted to read outside of this sequence (1.." + length() + ") at " + indx
      );
    }
    int i = findViewBlock(indx);
    if(i == -1) {
      return AlphabetManager.instance().getGapResidue();
    } else {
      Block b = (Block) blocks.get(i);
      return source.residueAt(b.sourceStart - b.viewStart + indx);
    }
  }
  
  public GappedResidueList(ResidueList source) {
    this.source = source;
    this.alpha = AlphabetManager.instance().getGappedAlphabet(source.alphabet());
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
   */
  private final class Block {
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
