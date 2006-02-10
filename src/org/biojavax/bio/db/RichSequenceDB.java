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

package org.biojavax.bio.db;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojavax.bio.seq.RichSequenceIterator;

/**
 * A database of RichSequences with accessible keys and iterators over all
 * sequences.
 * <p>
 * This may have several implementations with rich behaviour, but basically most
 * of the time you will just use the interface methods to do stuff. A sequence
 * database contains a finite number of sequences stored under unique keys.
 *
 * @author Matthew Pocock
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A>
 * @author Thomas Down
 * @author Richard Holland
 */
public interface RichSequenceDB extends SequenceDB,BioEntryDB,RichSequenceDBLite {
    /**
     * {@inheritDoc}
     * Will always return an instance of RichSequenceIterator.
     */
    public SequenceIterator sequenceIterator();
    
    /**
     * Returns a RichSequenceIterator over all sequences in the database. The order
     * of retrieval is undefined.
     * @return a RichSequenceIterator over all sequences
     */
    public RichSequenceIterator getRichSequenceIterator();
    
    /**
     * <p>Attempt to pre-process a FeatureFilter using database-specific methods, 
     * returning a FeatureHolder containing the results. Any FeatureFilter passed
     * which this method cannot pre-process should be treated as FeatureFilter.all.</p>
     * <p>The filter() method will post-process results returned by this method</p>
     * using the standard accept() technique. This two-pass approach guarantees that
     * all FeatureFilters will work correctly regardless of underlying database type,
     * and any optimisations that can take place at the database level will be done.</p>
     *
     * @param ff the FeatureFilter to attempt to pre-process
     * @return a FeatureHolder containing the pre-processed matching features.
     */
    public FeatureHolder preprocessFeatureFilter(FeatureFilter ff);
}
