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

import java.sql.*;
import java.util.*;

import org.biojava.utils.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

/**
 * Sequence keyed off a BioSQL biosequence.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.3
 */

class BioSQLSequence
  implements
    Sequence,
    RealizingFeatureHolder,
    BioSQLSequenceI
{
    private BioSQLSequenceDB seqDB;
    private String name;
    private int bioentry_id = -1;
    private int biosequence_id;
    private Annotation annotation;
    private Alphabet alphabet;
    private RealizingFeatureHolder features;
    private SymbolList symbols;
    private int length;

    private DBHelper getDBHelper() {
	return seqDB.getDBHelper();
    }

    public BioSQLSequenceDB getSequenceDB() {
	return seqDB;
    }

    public int getBioEntryID() {
	return bioentry_id;
    }

    BioSQLSequence(BioSQLSequenceDB seqDB,
	           String name,
	           int bioentry_id,
                   int biosequence_id,
                   String alphaName,
                   int length)
	throws BioException
    {
	this.seqDB = seqDB;
	this.name = name;
	this.bioentry_id = bioentry_id;
	this.biosequence_id = biosequence_id;
        this.length = length;

	try {
	    this.alphabet = AlphabetManager.alphabetForName(alphaName.toUpperCase());
	} catch (NoSuchElementException ex) {
	    throw new BioException(ex, "Can't load sequence with unknown alphabet " + alphaName);
	}

	// features = new BioEntryFeatureSet(this, seqDB, bioentry_id);
    }

    public String getName() {
	return name;
    }

    public String getURN() {
	return name;
    }

    //
    // implements Annotatable
    //

    public Annotation getAnnotation() {
	if (annotation == null) {
	    annotation = new BioSQLSequenceAnnotation(seqDB, bioentry_id);
	}

	return annotation;
    }

    //
    // implements SymbolList
    //

    public Alphabet getAlphabet() {
	return alphabet;
    }

    public int length() {
        if (length >= 0) {
            return length;
        } else {
	    return getSymbols().length();
        }
    }

    public Symbol symbolAt(int i) {
	return getSymbols().symbolAt(i);
    }

    public SymbolList subList(int start, int end) {
	return getSymbols().subList(start, end);
    }

    public List toList() {
	return getSymbols().toList();
    }

    public Iterator iterator() {
	return getSymbols().iterator();
    }

    public String seqString() {
	return getSymbols().seqString();
    }

    public String subStr(int start, int end) {
	return getSymbols().subStr(start, end);
    }

    public void edit(Edit e) 
        throws ChangeVetoException 
    {
	throw new ChangeVetoException("Can't edit sequence in BioSQL -- or at least not yet...");
    }    

    protected synchronized SymbolList getSymbols()
        throws BioRuntimeException
    {
	if (symbols == null) {
	    if (biosequence_id < 0) {
		throw new BioError("Assertion failed: can only lazy-fetch sequence if from a biosequence entry");
	    }

	    try {
		Connection conn = seqDB.getPool().takeConnection();
		
		PreparedStatement get_symbols = conn.prepareStatement("select biosequence_str " +
								      "from   biosequence " +
								      "where  biosequence_id = ?");
		get_symbols.setInt(1, biosequence_id);
		ResultSet rs = get_symbols.executeQuery();
		String seqString = null;
		if (rs.next()) {
		    seqString = rs.getString(1);  // FIXME should do something stream-y
                    if (rs.wasNull()) {
                        seqString = null;
                    }
		}
		get_symbols.close();

		seqDB.getPool().putConnection(conn);

		if (seqString != null) {
		    try {
			Alphabet alpha = getAlphabet();
			SymbolTokenization toke = alpha.getTokenization("token");
			symbols = new SimpleSymbolList(toke, seqString);
		    } catch (Exception ex) {
			throw new BioRuntimeException(ex, "Couldn't parse tokenized symbols");
		    }
		} else {
                    if (! (length >= 0)) {
                        throw new BioRuntimeException("Length not available from database");
                    }
                                
		    symbols = new DummySymbolList((FiniteAlphabet) alphabet, length);
		}
	    } catch (SQLException ex) {
		throw new BioRuntimeException(ex, "Unknown error getting symbols from BioSQL.  Oh dear.");
	    }
	}

	return symbols;
    }

    //
    // implements FeatureHolder
    //

    private RealizingFeatureHolder getFeatures() {
	if (features == null) {
	    features = new BioSQLTiledFeatures(this, seqDB, bioentry_id, 10000);
	}
	return features;
    }

    public Iterator features() {
	return getFeatures().features();
    }

    public int countFeatures() {
	return getFeatures().countFeatures();
    }

    public boolean containsFeature(Feature f) {
	return getFeatures().containsFeature(f);
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getFeatures().filter(ff, recurse);
    }

    public Feature createFeature(Feature.Template ft)
        throws ChangeVetoException, BioException
    {
	return getFeatures().createFeature(ft);
    }

    public void removeFeature(Feature f)
        throws ChangeVetoException
    {
	getFeatures().removeFeature(f);
    }

    public Feature realizeFeature(FeatureHolder parent, Feature.Template templ)
        throws BioException
    {
	return getFeatures().realizeFeature(parent, templ);
    }

    public void persistFeature(Feature f, int parent_id)
        throws BioException
    {
	seqDB.getFeaturesSQL().persistFeature(f, parent_id, bioentry_id);
    }

    public void addChangeListener(ChangeListener cl) {
	addChangeListener(cl, ChangeType.UNKNOWN);
    }
    
    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	getSequenceDB().getChangeHub().addEntryListener(bioentry_id, cl, ct);
    }

    public void removeChangeListener(ChangeListener cl) {
	removeChangeListener(cl, ChangeType.UNKNOWN);
    }

    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	getSequenceDB().getChangeHub().removeEntryListener(bioentry_id, cl, ct);
    }

    public boolean isUnchanging(ChangeType ct) {
	return false;
    }
}
