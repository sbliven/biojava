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

package org.biojava.bio.seq.distributed;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;

/**
 * Sequence database from the meta-DAS system.
 *
 * @author Thomas Down
 * @since 1.2
 */

public class DistributedSequenceDB extends AbstractSequenceDB implements SequenceDB {
    private Set datasources;

    {
	datasources = new HashSet();
    }

    public void addDataSource(DistDataSource dds) 
        throws ChangeVetoException
    {
	datasources.add(dds);
    }

    public void removeDataSource(DistDataSource dds) 
        throws ChangeVetoException
    {
	datasources.remove(dds);
    }

    public String getName() {
	return "<unknown meta-das>";
    }

    public void addSequence(Sequence seq)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't add sequences to meta-das");
    }

    public void removeSequence(String id)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't add sequences to meta-das");
    }

    public Sequence getSequence(String id)
        throws IllegalIDException, BioException
    {
	Set featureSources = new HashSet();
	DistDataSource seqSource = null;

	for (Iterator i = datasources.iterator(); i.hasNext(); ) {
	    DistDataSource dds = (DistDataSource) i.next();
	    if (dds.hasSequence(id) && seqSource == null) {
		seqSource = dds;
	    }

	    if (dds.hasFeatures(id)) {
		featureSources.add(dds);
	    }
	}

	if (seqSource == null) {
	    throw new IllegalIDException("No sequence source for ID: " + id);
	}

	return new DistributedSequence(id, this, seqSource, featureSources);
    }

    public Set ids() {
	Set ids = new HashSet();
	for (Iterator i = datasources.iterator(); i.hasNext(); ) {
	    DistDataSource dds = (DistDataSource) i.next();
	    try {
		ids.addAll(dds.ids(true));
	    } catch (BioException ex) {
	    }
	}
	return ids;
    }

    // 
    // Changeable stuff (which we'll cheat on for now...)
    //

    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
}
