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
 * Notification interface for objects which listen to a sequence stream
 * parser.
 *
 * @author Thomas Down
 * @since 1.1 [newio proposal]
 */

public interface SeqIOListener {
    /**
     * Start the processing of a sequence.  This method exists primarily
     * to enforce the life-cycles of SeqIOListener objects.
     */

    public void startSequence();

    /**
     * Notify the listener that processing of the sequence is complete.
     */

    public void endSequence();

    /**
     * Notify the listener that the current sequence is generally known
     * by a particular name.
     */

    public void setName(String name);

    /**
     * Notify the listener of a URI identifying the current sequence.
     */

    public void setURI(String uri);

    /**
     * Notify the listener of symbol data.  All symbols passed to
     * this method are guarenteed to be contained within the
     * specified alphabet.  Generally all calls to a given Listener
     * should have the same alphabet -- if not, the listener implementation
     * is likely to throw an exception
     *
     * @param alpha The alphabet of the symbol data
     * @param syms An array containing symbols
     * @param start The start offset of valid data within the array
     * @param length The number of valid symbols in the array
     *
     * @throws IllegalAlphabetException if we can't cope with this
     *                                  alphabet.
     */

    public void addSymbols(Alphabet alpha, Symbol[] syms, int start, int length)
        throws IllegalAlphabetException;

    /**
     * Notify the listener of a sequence-wide property.  This might
     * be stored as an entry in the sequence's annotation bundle.
     */

    public void addSequenceProperty(String key, Object value);

    /**
     * Notify the listener that a new feature object is starting.
     * Every call to startFeature should have a corresponding call
     * to endFeature.  If the listener is concerned with a hierarchy
     * of features, it should maintain a stack of `open' features.
     */

    public void startFeature(Feature.Template templ);

    /**
     * Mark the end of data associated with one specific feature.
     */

    public void endFeature();

    /**
     * Notify the listener of a feature property.
     */

    public void addFeatureProperty(String key, Object value);
}
