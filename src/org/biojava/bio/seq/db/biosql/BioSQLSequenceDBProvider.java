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

package org.biojava.bio.seq.db.biosql;

import java.util.*;

import org.biojava.directory.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.db.*;

public class BioSQLSequenceDBProvider implements SequenceDBProvider {
    public String getName() {
	return "biosql";
    }

    public SequenceDBLite getSequenceDB(Map config)
        throws RegistryException, BioException
    {
	String location = (String) config.get("location");
	if (location == null) {
	    throw new RegistryException("BioSQL databases require a `location' option");
	}

	String userName = (String) config.get("userName");
	if (userName == null) {
	    throw new RegistryException("BioSQL databases require a `userName' option");
	}

	String password = (String) config.get("password");
	if (password == null) {
	    password = "";
	}

	String biodatabase = (String) config.get("biodatabase");
	if (biodatabase == null) {
	    throw new RegistryException("BioSQL database require a `biodatabase' option");
	}

	return new BioSQLSequenceDB(location,
				    userName,
				    password,
				    biodatabase,
				    false);

    }
}
