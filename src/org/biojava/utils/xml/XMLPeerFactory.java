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
 * @(#)XMLPeerFactory.java      0.1 99/10/18
 *
 * (c) Thomas Down
 */

package org.biojava.utils.xml;

import org.xml.sax.*;
import java.util.*;

/**
 * Interface to an factory which produces Java objects which
 * reflect element in an XML document.
 *
 * @author Thomas Down
 */

public interface XMLPeerFactory {
    /**
     * Return a Java object to reflect an XML element.
     *
     * @param tag the XML tag.
     * @param children a List (may be empty) of objects reflecting
     *                 child nodes.
     * @param attrs attribute list for the tag.
     *
     * @return a Java object to reflect the XML tag, or <code>null</code> if
     *         this element should be silently ignored.
     */

    public Object getXMLPeer(String tag, List children,
			     AttributeList attrs);
}
