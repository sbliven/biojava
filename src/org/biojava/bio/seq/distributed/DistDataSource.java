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

package org.biojava.bio.seq.distributed;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * Object which contributes data to a DistSequenceDB.
 *
 * <p>
 * Probably changing pretty-much daily!
 * </p>
 *
 * @author Thomas Down
 * @since 1.2
 */

public interface DistDataSource {
    public boolean hasSequence(String id) throws BioException;
    public boolean hasFeatures(String id) throws BioException;

    public FeatureHolder getFeatures(FeatureFilter ff) throws BioException;
    public FeatureHolder getFeatures(String id, FeatureFilter ff, boolean recurse) throws BioException;
    public Sequence getSequence(String id) throws BioException;
    public Set ids(boolean topLevel) throws BioException;
}
