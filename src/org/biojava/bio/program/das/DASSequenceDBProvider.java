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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.db.SequenceDBLite;
import org.biojava.directory.RegistryException;
import org.biojava.directory.SequenceDBProvider;

/**
 * Hook DAS into the OBDA directory system.  Not for end users.
 *
 * @author Thomas Down
 * @since 1.3
 */

public class DASSequenceDBProvider implements SequenceDBProvider {
    public String getName() {
        return "das";
    }

    public SequenceDBLite getSequenceDB(Map config)
        throws RegistryException, BioException
    {
        String location = (String) config.get("location");
        if (location == null) {
            throw new RegistryException("DAS databases require a `location' option");
        }
        try {
            return new DASSequenceDB(new URL(location));
        } catch (MalformedURLException ex) {
            throw new RegistryException(ex, "Bad DAS URL");
        }
    }
}
