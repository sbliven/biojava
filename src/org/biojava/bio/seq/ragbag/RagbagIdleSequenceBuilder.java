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

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Symbol;

/**
 * A breakfast cereal of a SequenceBuilder,
 * jes' sits there soakin' up all those events
 * and doin' absolutely nuffin'.
 * 
 * @author David Huen (also doing sod all)
 * @version 1.2 [newio proposal]
 */

public class RagbagIdleSequenceBuilder implements SequenceBuilder {


    public void startSequence() {
    }

    public void endSequence() {
    }

    public void setName(String name) {
    }

    public void setURI(String uri) {
    }

    public void addSymbols(Alphabet alpha, Symbol[] syms, int pos, int len)
        throws IllegalAlphabetException
    {
    }

    public void addSequenceProperty(Object key, Object value) {
    }

    public void startFeature(Feature.Template templ) {
    }

    public void addFeatureProperty(Object key, Object value)
    throws ParseException {
    }

    public void endFeature() {
    }

    public Sequence makeSequence() {
      return (Sequence) null;
    }

    public void addProperty(Annotation ann, Object key, Object value) {
    }
}
