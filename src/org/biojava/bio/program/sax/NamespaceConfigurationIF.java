package org.biojava.bio.program.sax;

import java.util.*;

import org.xml.sax.SAXException;

/**
 * Support for SAX2 configuration of namespace support
 * <p>
 * Copyright &copy; 2000 Cambridge Antibody Technology Group plc.
 * All Rights Reserved.
 * <p>
 * Primary author -<ul>
 * <li>Simon Brocklehurst (CAT)
 * </ul>
 * Other authors  -<ul>
 * <li>Tim Dilks          (CAT)
 * <li>Colin Hardman      (CAT)
 * <li>Stuart Johnston    (CAT)
 *</ul>
 *
 * This code was first released to the biojava.org project, July 2000.
 *
 * @author Cambridge Antibody Technology Group plc (CAT)
 * @version 1.0
 *
 */
interface NamespaceConfigurationIF {

    /**
     * Support SAX2 configuration of namespace support of parser.
     */
    boolean getNamespaces();
    /**
     * Support SAX2 configuration of namespace support of parser.
     */
    public boolean getNamespacePrefixes();
    /**
     * Gets the URI for a namespace prefix, given that prefix,
     * or null if the prefix is not recognised.
     *
     * @param poPrefix a <code>String</code> The namespace prefix.
     */
    public String getURIFromPrefix(String poPrefix);


}

