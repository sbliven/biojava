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

package org.biojava.bio.seq.genomic;

import java.util.*;
import org.biojava.bio.seq.*;

/**
 * The root of a feature tree representing a transcribed region of a genome.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.1
 */

public interface Gene extends StrandedFeature {
    /**
     * Retrieve an unmodifiable set of exons that are 'part of' this gene.
     *
     * @return a Set of Exon features
     */
    public Set getExons();

    /**
     * The template for representing stranded features.
     *
     * @author Thomas Down
     */
    public static class Template extends StrandedFeature.Template {
    }
}
