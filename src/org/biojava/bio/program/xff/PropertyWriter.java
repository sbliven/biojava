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
package org.biojava.bio.program.xff;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.utils.xml.*;

/**
 * @author Thomas Down
 */
public class PropertyWriter extends BasicXFFHelper {
    public void writeDetails(XMLWriter xw, Feature f)
    throws IOException {
        Annotation a = f.getAnnotation();
        for (Iterator ai = a.keys().iterator(); ai.hasNext(); ) {
            Object key =  ai.next();
            if (! (key instanceof String))
                continue;
            Object value = a.getProperty(key);
            if (! (value instanceof String)) {
                continue;
            }
            
            xw.openTag("biojava:prop");
            xw.attribute("key", (String) key);
            xw.print((String) value);
            xw.closeTag("biojava:prop");
        }
    }
}
