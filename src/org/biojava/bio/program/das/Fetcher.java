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

package org.biojava.bio.program.das;

import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.utils.stax.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.xff.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Encapsulate a single batch of feature requests to a DAS server.
 *
 * @since 1.2
 * @author Thomas Down
 * @author David Huen
 */

interface Fetcher {
    URL getDataSourceURL();
    void addTicket(FeatureRequestManager.Ticket ticket);
    int size();
    List getDoneTickets();
    void runFetch() throws BioException, ParseException ;
}

