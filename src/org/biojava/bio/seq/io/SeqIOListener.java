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
     * Notify the listener of symbol data.
     *
     * <p>
     * NOTE: The SymbolReader is only guarenteed to be valid within
     * this call.  If the listener does not fully read all the data,
     * the parser <em>may</em> assume that it is not required, and
     * skip it.
     * </p>
     */

    public void addSymbols(SymbolReader sr)
        throws IOException, IllegalSymbolException;

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
