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

package org.biojava.bio.program.ssbind;

import org.xml.sax.ContentHandler;

/**
 * <code>HandlerBinding</code>s associate an identifier (e.g. an XML
 * element name) with an XML <code>ContentHandler</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
class HandlerBinding
{
    String         name;
    ContentHandler handler;

    /**
     * Creates a new <code>HandlerBinding</code>.
     *
     * @param name a <code>String</code> identifier.
     * @param handler a <code>ContentHandler</code>.
     */
    HandlerBinding(String name, ContentHandler handler)
    {
        this.name    = name;
        this.handler = handler;
    }
}
