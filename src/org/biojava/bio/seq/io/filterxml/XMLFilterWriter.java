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

package org.biojava.bio.seq.io.filterxml;

import java.io.*;
import org.biojava.bio.seq.*;
import org.biojava.utils.xml.*;

/**
 * Interface for writing <code>FeatureFilters</code> as XML.  Implement
 * this if you want to enable XML serialization of a new type of <code>FeatureFilters</code>.
 *
 * <p>
 * To serialize one of the built-in FeatureFilters, use <code>XMLFilterWriterConfig</code>.
 * </p>
 *
 * @author Thomas Down
 * @since 1.3
 */

public interface XMLFilterWriter {
    public void writeFilter(FeatureFilter ff,
                            XMLWriter xw,
                            XMLFilterWriterConfig config)
        throws ClassCastException, IllegalArgumentException, IOException;
}

