/**
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
 
package org.biojava.bio.seq.ragbag;
 
import java.util.Iterator;
import java.util.List;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
 
/**
 * object that instantiates a sequence in Ragbag.
 * It accepts a single sequence file and any number
 * of feature files that are to be applied to that
 * sequence.
 *
 * @author David Huen
 * @author Matthew Pocock
 * @since 1.2
 */
class RagbagAbstractSequence implements Sequence
{
  // class variables
  protected Sequence sequence;

  // delegate everything to sequence object
  // I do this so I can later implement some kind of caching and cache flush.
  public String getName() {return sequence.getName();}
  public String getURN() {return sequence.getURN();}
 
  public void edit(Edit edit) throws IllegalAlphabetException, ChangeVetoException {sequence.edit(edit);}
  public Alphabet getAlphabet() {return sequence.getAlphabet();}
  public Iterator iterator() {return sequence.iterator();}
  public int length() {return sequence.length();}
  public String seqString() {return sequence.seqString();}
  public SymbolList subList(int start, int end) {return sequence.subList(start, end);}
  public String subStr(int start, int end) {return sequence.subStr(start, end);}
  public Symbol symbolAt(int index) {return sequence.symbolAt(index);}
  public List toList() {return sequence.toList();}
 
  public void addChangeListener(ChangeListener cl) {sequence.addChangeListener(cl);}
  public void addChangeListener(ChangeListener cl, ChangeType ct) {sequence.addChangeListener(cl, ct);}
  public void removeChangeListener(ChangeListener cl) {sequence.addChangeListener(cl);}
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {sequence.addChangeListener(cl, ct);}
  public boolean isUnchanging(ChangeType ct) {return sequence.isUnchanging(ct);}
 
  public boolean containsFeature(Feature f) {return sequence.containsFeature(f);}
  public int countFeatures() {return sequence.countFeatures();}
  public Feature createFeature(Feature.Template ft) throws BioException, ChangeVetoException
    {return sequence.createFeature(ft);}
  public Iterator features() {return sequence.features();}
  public FeatureHolder filter(FeatureFilter ff) {return sequence.filter(ff);}
  public FeatureHolder filter(FeatureFilter ff, boolean recurse) {return sequence.filter(ff, recurse);}
  public void removeFeature(Feature f) throws ChangeVetoException, BioException {sequence.removeFeature(f);}
  public FeatureFilter getSchema() {return sequence.getSchema();}
 
  public Annotation getAnnotation() {return sequence.getAnnotation();}
}

