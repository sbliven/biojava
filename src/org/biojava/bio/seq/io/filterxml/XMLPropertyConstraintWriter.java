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
import java.util.*;
import java.lang.reflect.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.utils.xml.*;

/**
 * Interface for writing <code>PropertyConstraints</code> as XML.  Implement
 * this if you want to enable XML serialization of a new type of <code>PropertyConstraint</code>.
 *
 * @author Thomas Down
 * @since 1.3
 */
 
public interface XMLPropertyConstraintWriter {
    public void writePropertyConstraint(PropertyConstraint pc,
                                        XMLWriter xw,
                                        XMLAnnotationTypeWriter config)
        throws ClassCastException, IOException, IllegalArgumentException;
}
