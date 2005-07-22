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

 * PersistentComparableOntology.java

 *

 * Created on June 16, 2005, 2:30 PM

 */



package org.biojavax.bio.db;

import java.sql.SQLException;

import java.util.Collections;

import java.util.HashSet;

import java.util.Iterator;

import java.util.NoSuchElementException;

import java.util.Set;

import org.biojava.bio.BioError;

import org.biojava.ontology.AlreadyExistsException;

import org.biojava.ontology.Term;

import org.biojava.ontology.Triple;

import org.biojava.utils.ChangeVetoException;

import org.biojavax.ontology.ComparableOntology;

import org.biojavax.ontology.SimpleComparableOntology;



/**

 * Represents an ontology that can be compared to other ontologies.

 *

 * Equality is based on name alone.

 *

 * @author Richard Holland

 * @author Mark Schreiber

 */

public abstract class PersistentComparableOntology extends SimpleComparableOntology implements Persistent {

    

    private int status;

    private int uid;

    private PersistentBioDB db;

    

    protected PersistentComparableOntology(PersistentBioDB db, String name) {

        super(name);

        this.status = Persistent.UNMODIFIED;

        this.uid = Persistent.UID_UNKNOWN;

        this.db = db;

    }

    

    protected PersistentComparableOntology(PersistentBioDB db, ComparableOntology o) {

        this(db, o.getName());

        try {

            this.setDescription(o.getDescription());

            for (Iterator i = o.getTerms().iterator(); i.hasNext(); ) {

                Term t = (Term)i.next();

                this.createTerm(t.getName(), t.getDescription(), t.getSynonyms());

            }

            for (Iterator i = o.getTriples(null,null,null).iterator(); i.hasNext(); ) {

                Triple t = (Triple)i.next();

                this.createTriple(t.getSubject(), t.getObject(), t.getPredicate(), t.getName(), t.getDescription());

            }

            // We cowardly refuse to deal with ontology variable as we haven't a clue what they are.

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
        
        if (status==Persistent.MODIFIED) {
            this.addedThings.clear();
            this.removedThings.clear();
        }

    }

    

    public void setUid(int uid) {

        this.uid = uid;

    }

    

    public abstract Persistent load(Object[] vars) throws Exception;

    

    public abstract boolean remove(Object[] vars) throws Exception;

    

    public abstract Persistent store(Object[] vars) throws Exception;

    

    public void setDescription(String description) throws ChangeVetoException {

        super.setDescription(description);

        this.status = Persistent.MODIFIED;

    }



    // Contains all names removed since the last commit, EXCEPT those which

    // were added AND removed since the last commit. It is up to the subclass

    // to reset this else it could get inconsistent.

    private Set removedThings = new HashSet();

    protected Set getRemovedThings() { return Collections.unmodifiableSet(this.removedThings); }

    // Contains all names added since the last commit, EXCEPT those which

    // were added AND removed since the last commit. It is up to the subclass

    // to reset this else it could get inconsistent.

    private Set addedThings = new HashSet();

    protected Set getAddedThings() { return Collections.unmodifiableSet(this.addedThings); }

    

    public Term createTerm(String name, String description, Object[] synonyms) throws AlreadyExistsException, ChangeVetoException, IllegalArgumentException {

        Term t = super.createTerm(name, description, synonyms);

        PersistentComparableTerm pt = (PersistentComparableTerm)this.db.convert(t);

        this.addedThings.add(pt);

        this.removedThings.remove(pt);

        this.status = Persistent.MODIFIED;

        return pt;

    }

    

    public Term importTerm(Term t, String localName) throws ChangeVetoException, IllegalArgumentException {

        Term it = super.importTerm(t, localName);            

        PersistentComparableTerm pt = (PersistentComparableTerm)this.db.convert(it);    

        this.addedThings.add(pt);

        this.removedThings.remove(pt);

        this.status = Persistent.MODIFIED;

        return pt;

    }

    

    public Triple createTriple(Term subject, Term object, Term predicate, String name, String description) throws AlreadyExistsException, ChangeVetoException {

        Triple t = super.createTriple(subject, object, predicate, name, description);      

        PersistentComparableTriple pt = (PersistentComparableTriple)this.db.convert(t);    

        this.addedThings.add(pt);

        this.removedThings.remove(pt);

        this.status = Persistent.MODIFIED;

        return pt;

    }

    

    public void deleteTerm(Term t) throws ChangeVetoException {

        super.deleteTerm(t);

        this.removedThings.add(t);

        this.addedThings.remove(t);

        this.status = Persistent.MODIFIED;

    }

        

    public Term createTerm(String name, String description) throws AlreadyExistsException, ChangeVetoException, IllegalArgumentException {

        return this.createTerm(name,description,null);

    }



    public Term getTerm(String s) throws NoSuchElementException {

        Term t = super.getTerm(s);

        PersistentComparableTerm pt = (PersistentComparableTerm)this.db.convert(t);    

        return pt;

    }



    public Set getTriples(Term subject, Term object, Term predicate) {

        Set pts = new HashSet();

        for (Iterator i = super.getTriples(subject, object, predicate).iterator(); i.hasNext(); ) 

            pts.add(this.db.convert(i.next()));

        return pts;

    }



    public Set getTerms() {

        Set pts = new HashSet();

        for (Iterator i = super.getTerms().iterator(); i.hasNext(); ) pts.add(this.db.convert(i.next()));

        return pts;

    }

    

}

