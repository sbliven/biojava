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

package org.biojava.bio.seq.io;

import java.io.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

/**
 * Adapter class for SeqIOListener that has empty methods.
 *
 * @author Matthew Pocock
 * @since 1.1
 */

public class SeqIOAdapter implements SeqIOListener {
  public void startSequence()                                   {}
  public void endSequence()                                     {}
  public void setName(String name)                              {}
  public void setURI(String uri)                                {}
  public void addSymbols(Alphabet alpha, Symbol[] syms, int start, int length)
  throws IllegalAlphabetException                               {}
  public void addSequenceProperty(String key, Object value)     {}
  public void startFeature(Feature.Template templ)              {}
  public void endFeature()                                      {}
  public void addFeatureProperty(String key, Object value)      {}
}
