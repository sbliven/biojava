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

import java.util.Iterator;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.db.AbstractSequenceDB;
import org.biojava.bio.seq.db.IllegalIDException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;

/**
 * An abstract implementation of RichSequenceDB that provides the getRichSequenceIterator
 * method.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author Richard Holland
 */
public abstract class AbstractRichSequenceDB extends AbstractSequenceDB implements RichSequenceDB {
    
    public SequenceIterator sequenceIterator() {
        return this.getRichSequenceIterator();
    }
    
    public Sequence getSequence(String id) throws BioException, IllegalIDException {
        return this.getRichSequence(id);
    }
    
    public RichSequenceIterator getRichSequenceIterator() {
        return new RichSequenceIterator() {
            private Iterator pID = ids().iterator();
            
            public boolean hasNext() {
                return pID.hasNext();
            }
            
            public Sequence nextSequence() throws BioException {
                return nextRichSequence();
            }
            
            public RichSequence nextRichSequence() throws BioException {
                return getRichSequence((String)pID.next());
            }
        };
    }
}
