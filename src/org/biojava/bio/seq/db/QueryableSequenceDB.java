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

package org.biojava.bio.seq.db;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * An extention to SequenceDB that allows features to be extracted using a FeatureFilter.
 * <p>
 * <em>Note:</em> This is transient and unsupported API, and may be removed or replaced at any time.
 *
 * @author Matthew Pocock
 */
public interface QueryableSequenceDB extends SequenceDB {
  public FeatureHolder filterFeatures(FeatureFilter filter, boolean recurse);
}
