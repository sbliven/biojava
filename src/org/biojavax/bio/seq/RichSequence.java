/*
 * RichSequence.java
 *
 * Created on July 22, 2005, 5:17 PM
 *
 */

package org.biojavax.bio.seq;

import org.biojava.bio.seq.Sequence;
import org.biojavax.bio.BioEntry;

/**
 * A rich sequence is a combination of a <code>org.biojavax.bio.Bioentry</code>
 * and a <code>org.biojava.seq.Sequence</code>. It inherits and merges the methods
 * of both. The <code>RichSequence</code> is based on the BioSQL model and
 * provides a richer array of methods to access information than <code>Sequence</code>
 * does. The interface introduces no new methods of it's own. It is essentially
 * a <code>BioEntry</code> with sequence information.
 * <p>
 * Whenever possible <code>RichSequence</code> should be used in preference to
 * <code>Sequence</code>
 * @author Mark Schreiber
 */
public interface RichSequence extends Sequence, BioEntry{}
