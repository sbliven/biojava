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
import java.util.*;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Factory for new latent features.  This is currently used
 * by the EMBL and GENBANK parsers to allow customisable mapping
 * from the attribute lists in the feature table to BioJava
 * features.
 *
 * @author Thomas Down
 */

public interface FeatureBuilder {
    /**
     * Return a latent (template) feature object to represent
     * a feature in some database.
     *
     * @param type String which gives the feature's type.
     * @param loc The feature's location in its parent sequence.
     * @param strandHint a value from StrandedFeature hinting
     *                   at which strand of DNA the caller
     *                   believes the feature to be on.
     *                   This may be safely ignored if you
     *                   believe that the notion of `strand'
     *                   is not relevant.
     * @param attrs a Map where the key-value pairs represent
     *              attributes of the feature.  In general,
     *              the keys will always be Strings, and the
     *              values will usually be Strings, but may
     *              be other Objects.  For an attribute with
     *              no value, Boolean.TRUE is used.
     *
     * @return A Feature.Template instance
     */

    public Feature.Template buildFeatureTemplate(String type,
						 Location loc,
						 StrandedFeature.Strand strandHint,
						 Map attrs);
}
