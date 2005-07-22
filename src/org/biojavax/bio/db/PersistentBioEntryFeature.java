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

 * PersistentBioEntryFeature.java

 *

 * Created on June 16, 2005, 11:47 AM

 */



package org.biojavax.bio.db;

import java.sql.SQLException;

import java.util.Collections;

import java.util.HashSet;

import java.util.Set;

import org.biojava.bio.BioError;

import org.biojava.bio.BioException;

import org.biojava.bio.seq.Feature;

import org.biojava.bio.seq.FeatureHolder;

import org.biojava.bio.seq.Sequence;

import org.biojava.bio.seq.StrandedFeature;

import org.biojava.bio.symbol.Location;

import org.biojava.ontology.Term;

import org.biojava.utils.ChangeVetoException;

import org.biojavax.bio.BioEntryFeature;

import org.biojavax.bio.SimpleBioEntryFeature;





/**

 * A simple implementation of BioEntryFeature.

 *

 * Equality is inherited from SimpleStrandedFeature.

 *

 * @author Richard Holland

 * @author Mark Schreiber

 */

public abstract class PersistentBioEntryFeature extends SimpleBioEntryFeature implements Persistent {

    

    private int status;

    private int uid;

    private PersistentBioDB db;

    

    public PersistentBioEntryFeature(PersistentBioDB db, Sequence sourceSeq, FeatureHolder parent, StrandedFeature.Template template) {

        super(sourceSeq, parent, template);

        this.status = Persistent.UNMODIFIED;

        this.uid = Persistent.UID_UNKNOWN;

        this.db = db;

    }

    

    protected PersistentBioEntryFeature(PersistentBioDB db, BioEntryFeature f) {

        this(db, f.getSequence(), f.getParent(), (StrandedFeature.Template)f.makeTemplate());

        try {

            this.setLocation(f.getLocation());

            // no setSource because it'll probably throw an exception at us

            this.setSourceTerm(f.getSourceTerm());

            this.setStrand(f.getStrand());

            this.setType(f.getType());

            this.setTypeTerm(f.getTypeTerm());

        } catch (ChangeVetoException e) {

            throw new BioError("Whoops! Parent class does not understand its own data!");

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
            this.addedFeatures.clear();
            this.removedFeatures.clear();
        }

    }

    

    public void setUid(int uid) {

        this.uid = uid;

    }

    

    public abstract Persistent load(Object[] vars) throws Exception;

    

    public abstract boolean remove(Object[] vars) throws Exception;

    

    public abstract Persistent store(Object[] vars) throws Exception;

    

    public void setType(String type) throws ChangeVetoException {

        super.setType(type);

        this.status = Persistent.MODIFIED;

    }

    

    public void setSource(String source) throws ChangeVetoException {

        super.setSource(source);

        this.status = Persistent.MODIFIED;

    }

    

    public void setStrand(StrandedFeature.Strand strand) throws ChangeVetoException {

        super.setStrand(strand);

        this.status = Persistent.MODIFIED;

    }

    

    public void setLocation(Location loc) throws ChangeVetoException {

        super.setLocation(loc);

        this.status = Persistent.MODIFIED;

    }

    

    public void setTypeTerm(Term t) throws ChangeVetoException {

        super.setTypeTerm((PersistentComparableTerm)this.db.convert(t));

        this.status = Persistent.MODIFIED;

    }

    

    public void setSourceTerm(Term t) throws ChangeVetoException {

        super.setSourceTerm((PersistentComparableTerm)this.db.convert(t));

        this.status = Persistent.MODIFIED;

    }

    

    // Contains all names removed since the last commit, EXCEPT those which

    // were added AND removed since the last commit. It is up to the subclass

    // to reset this else it could get inconsistent.

    private Set removedFeatures = new HashSet();

    protected Set getRemovedFeatures() { return Collections.unmodifiableSet(this.removedFeatures); }

    // Contains all names added since the last commit, EXCEPT those which

    // were added AND removed since the last commit. It is up to the subclass

    // to reset this else it could get inconsistent.

    private Set addedFeatures = new HashSet();

    protected Set getAddedFeatures() { return Collections.unmodifiableSet(this.addedFeatures); }


    

    public Feature createFeature(Feature.Template ft) throws BioException, ChangeVetoException {

        Feature f = super.createFeature(ft);

        this.removedFeatures.remove(f);

        this.addedFeatures.add(f);

        this.status = Persistent.MODIFIED;

        return f;

    }

    

    public Feature realizeFeature(FeatureHolder fh, Feature.Template templ) throws BioException {

        Feature f = super.realizeFeature(fh, templ);

        if (this.getParent().equals(fh)) {

            this.removedFeatures.remove(f);

            this.addedFeatures.add(f);

            this.status = Persistent.MODIFIED;

        }

        return f;

    }

    

    public void removeFeature(Feature f) throws ChangeVetoException {

        super.removeFeature(f);

        this.removedFeatures.add(f);

        this.addedFeatures.remove(f);

        this.status = Persistent.MODIFIED;

    }

}

