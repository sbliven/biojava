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

package org.biojava.bio.seq.db.biofetch;

import java.util.*;

import org.biojava.directory.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.db.*;

/**
 * Directory-services plugin for biofetch databases.
 *
 * @for.developer This class is instantiated automatically by the
 *                directory-services code, and is not of direct
 *                interest to users.
 *
 * @author Thomas Down
 * @since 1.3
 */

public class BioFetchSequenceDBProvider implements SequenceDBProvider {
    public String getName() {
	return "biofetch";
    }

    public SequenceDBLite getSequenceDB(Map config)
        throws RegistryException, BioException
    {
	String prefix = (String) config.get("location");
	if (prefix == null) {
	    throw new RegistryException("BioFetch requires prefix");
	}

	String type = (String) config.get("format");
	if (type == null) {
	    throw new RegistryException("BioFetch requires type");
	}

	String db = (String) config.get("biodatabase");
	if (db == null) {
	    throw new RegistryException("BioFetch requires biodatabase paramter");
	}

	return new BioFetchSequenceDB(prefix, type, db);
    }
}
