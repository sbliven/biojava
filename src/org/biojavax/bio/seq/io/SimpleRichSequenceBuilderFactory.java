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

/*
 * SimpleRichSequenceBuilderFactory.java
 *
 * Created on August 5, 2005, 10:55 AM
 */

package org.biojavax.bio.seq.io;

import org.biojava.bio.seq.io.SequenceBuilder;


/**
 *
 * @author Mark Schreiber
 * @author Richard Holland
 */
public class SimpleRichSequenceBuilderFactory implements RichSequenceBuilderFactory {
    
    /** Creates a new instance of SimpleRichSequenceBuilderFactory */
    public SimpleRichSequenceBuilderFactory() {
    }

    public SequenceBuilder makeSequenceBuilder() {
        return new SimpleRichSequenceBuilder();
    }
    
}
