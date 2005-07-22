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

 * PersistentCrossRef.java

 *

 * Created on June 15, 2005, 5:32 PM

 */



package org.biojavax.bio.db;

import java.sql.SQLException;

import java.util.Collections;

import java.util.HashSet;

import java.util.List;

import java.util.Set;

import org.biojava.bio.BioError;

import org.biojava.ontology.AlreadyExistsException;

import org.biojava.utils.ChangeVetoException;

import org.biojavax.CrossRef;

import org.biojavax.SimpleCrossRef;

import org.biojavax.ontology.ComparableTerm;



/**

 * A basic CrossRef implementation.

 *

 * Equality is the dbname, accession and version combination.

 *

 * @author Richard Holland

 * @author Mark Schreiber

 */

public abstract class PersistentCrossRef extends SimpleCrossRef implements Persistent {

        

    private int status;

    private int uid;

    private PersistentBioDB db;

    

    protected PersistentCrossRef(PersistentBioDB db, String dbname, String accession, int version) {

        super(dbname,accession,version);

        this.status = Persistent.UNMODIFIED;

        this.uid = Persistent.UID_UNKNOWN;

        this.db = db;

    }

    

    protected PersistentCrossRef(PersistentBioDB db, CrossRef cr) {

        this(db,cr.getDbname(),cr.getAccession(),cr.getVersion());

        try {

            List terms = cr.getTerms();

            for (int i = 0; i < terms.size(); i++) this.setTerm((ComparableTerm)terms.get(i), cr.getTermValue(i), i);

        } catch (ChangeVetoException e) {

            throw new BioError("Whoops! Parent class does not understand its own data!");

        } catch (AlreadyExistsException e) {

            throw new BioError("Reality alert! Duplicates found in duplicate-free set!");

        }

    }

    

    public PersistentBioDB getDB() {

        return this.db;

    }

    

    public int getStatus() {

        return this.status;

    }

    

    public int getUid() {

        return this.uid;

    }

    

    public void setStatus(int status) throws IllegalArgumentException {

        if (status!=Persistent.UNMODIFIED && status!=Persistent.MODIFIED && status!=Persistent.DELETED)

            throw new IllegalArgumentException("Invalid status code");

        this.status = status;
        
        if (status==Persistent.UNMODIFIED) this.alteredTerms.clear();

    }

    

    public void setUid(int uid) {

        this.uid = uid;

    }

    

    public abstract Persistent load(Object[] vars) throws Exception;

    

    public abstract boolean remove(Object[] vars) throws Exception;

    

    public abstract Persistent store(Object[] vars) throws Exception;

    

    // Contains all names removed since the last commit, EXCEPT those which

    // were added AND removed since the last commit. It is up to the subclass

    // to reset this else it could get inconsistent.

    private Set alteredTerms = new HashSet();

    protected Set getAlteredTerms() { return Collections.unmodifiableSet(this.alteredTerms); }
    

    public void setTerm(ComparableTerm term, String value, int index) throws org.biojava.ontology.AlreadyExistsException, IllegalArgumentException, ChangeVetoException {

        PersistentComparableTerm pt = (PersistentComparableTerm)this.db.convert(term);

        super.setTerm(pt, value, index);

        this.alteredTerms.add(Integer.valueOf(index));

        this.status = Persistent.MODIFIED;

    }

    

}

