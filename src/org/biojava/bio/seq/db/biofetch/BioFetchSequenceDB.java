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
import java.io.*;
import java.net.*;

import org.biojava.utils.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;

/**
 * Simple SequenceDB implementation backed by a BioFetch (HTTP)
 * server
 *
 * @author Thomas Down
 * @since 1.3
 */

public class BioFetchSequenceDB implements SequenceDBLite {
    private final String prefix;
    private final String type;
    private final String db;
    
    public BioFetchSequenceDB(String prefix,
			      String type,
			      String db)
    {
	this.prefix = prefix;
	this.type = type;
	this.db = db;
    }

    public String getName() {
	return db;
    }

    
    public void addSequence(Sequence seq)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't add sequences to XEMBL");
    }

    public void removeSequence(String id)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't remove sequences from XEMBL");
    }

    public Sequence getSequence(String id)
        throws BioException, IllegalIDException
    {
	StringBuffer uri = new StringBuffer(prefix);
	uri.append('?');
	uri.append("style=raw;");
	uri.append("format=");
	uri.append(type);
	uri.append(";db=");
	uri.append(db);
	uri.append(";id=");
	uri.append(id);
	
	try {
	    HttpURLConnection huc = (HttpURLConnection) new URL(uri.toString()).openConnection();
	    huc.connect();
	    BufferedReader data = new BufferedReader(new InputStreamReader(huc.getInputStream()));
	    data.mark(1000);
	    String firstLine = data.readLine();
	    if (firstLine.startsWith("Content-")) {
		data.readLine();
		firstLine = data.readLine();
	    }
	    StringTokenizer toke = new StringTokenizer(firstLine);
	    String first = toke.nextToken();
	    if ("ERROR".equals(first)) {
		int errorCode = Integer.parseInt(toke.nextToken());
		if (errorCode == 4) {
		    throw new IllegalIDException("No such ID " + id + " in database " + getName());
		} else {
		    throw new BioException("Error fetching from BioFetch: firstLine");
		}
	    } 

	    data.reset();
	    if ("embl".equals(type)) {
		SequenceIterator si = SeqIOTools.readEmbl(data);
		return si.nextSequence();
	    } else {
		throw new BioException("Unknown format " + type);
	    }
	} catch (IOException ex) {
	    throw new BioException(ex, "Error reading data from BioFetch");
	}
    }
    
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void addChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
}
