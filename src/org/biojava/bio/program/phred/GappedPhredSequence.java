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

 package org.biojava.bio.program.phred;

 import org.biojava.bio.*;
 import org.biojava.bio.seq.*;
 import org.biojava.bio.seq.impl.*;
 import org.biojava.utils.*;
 import org.biojava.bio.symbol.*;

 import java.util.*;

/**
 * Title:        GappedPhredSequence
 * Description:  A Class that wraps two GappedSymbolLists, one for sequence data and one for
 *               quality data. All changes in sequence are reflected in the quality data. The two
 *               SymbolLists can be though of as a single sequence. The class provides a safer
 *               alternative to building a PhredSequence with two GappedSymbolLists (although this
 *               can be done).<p>
 *               The class may be particularly useful for sequence assembly projects where the
 *               quality of the sequence information must be taken into account.<p>
 *               A Number of the methods are <code>synchronized</code> to add integrety to the
 *               underlying symbol lists. If performance rather than saftey is an issue then
 *               implement a PhredSequence with two GappedSymbolLists and take care!
 *
 * Copyright:    Copyright (c) 2001
 * Company:      AgResearch
 * @author Mark Schreiber
 * @since 1.2
 */

public class GappedPhredSequence extends PhredSequence {
  private GappedSymbolList seq;
  private GappedSymbolList qual;

  /**
   * Create a new GappedPhredSequence.
   * @throws BioException if the sequence and quality parameters differer in length or gap position
   */
  public GappedPhredSequence(GappedSymbolList sequence,
                             GappedSymbolList quality,
                             String name,
                             String urn,
                             Annotation anno)throws BioException {

    super(sequence,quality,name,urn,anno);
    this.seq = sequence;
    this.qual = quality;
    if(badIntegrity()){
      throw new BioException("Sequence and Quality information differ in Length and/or Gap position");
    }
  }

  /**
   * Create a new GappedPhredSequence.
   * @throws BioException if the sequence and quality parameters differer in length or gap position
   */
  public GappedPhredSequence(SymbolList sequence,
                             SymbolList quality,
                             String name,
                             String urn,
                             Annotation anno)throws BioException{
    this(new GappedSymbolList(sequence),new GappedSymbolList(quality),name,urn,anno);
  }

  /**
   * Add a single gap at pos within the source coordintates.
   * <P>
   * this.symbolAt(pos) will then return gap. Adding a gap at 1 will prepend gaps. Adding
   * a gap at (length+1) will append a gap.
   *
   * @param pos  the position to add a gap before
   * @throws IndexOutOfBoundsException if pos is not within 1->length+1
   */
  public synchronized void addGapInSource(int pos){
    seq.addGapInSource(pos);
    qual.addGapInSource(pos);
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
  public synchronized void addGapInView(int pos){
    seq.addGapInView(pos);
    qual.addGapInView(pos);
  }

  /**
   * Add length gaps at pos within the view coordinates.
   *
   * @param pos where to add the gap
   * @param length  how many gaps to add
   * @throws IndexOutOfBoundsException if pos is not within 1->source.length()
   */
  public synchronized void addGapsinView(int pos, int length){
    seq.addGapsInView(pos, length);
    qual.addGapsInView(pos,length);
  }

  /**
   * Add length gaps at pos within the source coordinates.
   *
   * @param pos where to add the gap
   * @param length  how many gaps to add
   * @throws IndexOutOfBoundsException if pos is not within 1->source.length()
   */
  public synchronized void addGapsinSource(int pos, int length){
    seq.addGapsInSource(pos, length);
    qual.addGapsInSource(pos,length);
  }

  /**
   * Checks to see if the sequences are of equal length and have equal gap positions.
   * @return true if either condition is false.
   */
  private synchronized boolean badIntegrity(){
    //test for equal length
    if(seq.length() != qual.length()) return true;
    //test for equal gap positions
    if(gapPositions(seq).equals(gapPositions(qual))) return false;
    else return true;
  }

  /**
   * Return the index of the first Symbol that is not a Gap character.
   * <P>
   * All symbols before firstNonGap are leading gaps. firstNonGap is effectively
   * the index in the view of symbol 1 in the original sequence.
   *
   * @return the index of the first character not to be a gap
   */
  public synchronized int firstNonGap(){
    int x =  seq.firstNonGap();
    int y = qual.firstNonGap();
    if(x == y) return x;
    else throw new BioError("Sequence and Quality Information are not Equally Gapped");
  }

  /**
   * Locates the positions of all gaps in a sequence
   * @return a list of <code>Integer</code> objects that correspond to the positions of the gaps using
   * sequence numbering.
   */
  private synchronized List gapPositions(GappedSymbolList s){
    List gapPos = new ArrayList();
    for(int i = 1; i <= s.length(); i++){
      if(s.symbolAt(i).equals(s.getAlphabet().getGapSymbol())) gapPos.add(new Integer(i));
    }
    return gapPos;
  }

  /**
   * @return the alphabet of the sequence data.
   */
  public Alphabet getSequenceAlphabet(){
    return seq.getAlphabet();
  }

  /**
   * @return the alphabet of the quality data.
   */
  public Alphabet getQualityAlphabet(){
    return qual.getAlphabet();
  }
  public synchronized int lastNonGap(){
    int x =  seq.lastNonGap();
    int y = qual.lastNonGap();
    if(x == y) return x;
    else throw new BioError("Sequence and Quality Information are not Equally Gapped");
  }

  /**
   * The length of the sequence
   * @throws BioError if the quality and biological sequences do not agree.
   */
  public synchronized int length(){
    int x = seq.length();
    int y = qual.length();
    if(x == y) return x;
    else throw new BioError("Sequence and Quality Information are not Equally in Length");
  }

  /**
   * Remove a single gap at position pos in this GappedSymbolList.
   *
   * @param pos where to remove the gap
   * @throws IndexOutOfBoundsException if pos is not within 1..length
   * @throws IllegalSymbolException if the symbol at pos is not a gap
   */
  public synchronized void removeGap(int pos) throws IndexOutOfBoundsException, IllegalSymbolException{
    seq.removeGap(pos);
    qual.removeGap(pos);
  }

  /**
   * Remove some gaps at position pos.
   *
   * @param pos where to remove the gaps
   * @param length how many to remove
   * @throws IndexOutOfBoundsException if pos is not within 1..length() or if
   *         some of the Symbols within pos->(pos+length-1) are not gap Symbols
   * @throws IllegalSymbolException if the symbol at pos is not a gap
   */
  public synchronized void removeGaps(int pos, int length)throws IndexOutOfBoundsException, IllegalSymbolException{
    seq.removeGaps(pos,length);
    qual.removeGaps(pos,length);
  }

  /**
   * Converts source coordinates to view coordinates.
   * @throws BioError if the quality and biological sequences do not agree.
   */
  public synchronized int sourceToView(int index){
    int x = seq.sourceToView(index);
    int y = qual.sourceToView(index);
    if(x == y) return x;
    else throw new BioError("Sequence and Quality Information are not Equally Gapped");
  }

  /**
   * @return the symbol from the biological sequence at the specified index
   */
  public Symbol seqSymbolAt(int index){
    return seq.symbolAt(index);
  }

  /**
   * @return the symbol from the quality sequence at the specified index
   */
  public Symbol qualSymbolAt(int index){
    return qual.symbolAt(index);
  }
  public synchronized int viewToSource(int index){
    int x = seq.viewToSource(index);
    int y = qual.viewToSource(index);
    if(x == y) return x;
    else throw new BioError("Sequence and Quality Information are not Equally Gapped");
  }
}