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
 * PersistentNCBITaxon.java
 *
 * Created on June 16, 2005, 10:01 AM
 */

package org.biojavax.bio.db;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.biojava.bio.BioError;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.bio.taxa.SimpleNCBITaxon;

/**
 * Reference implementation of NCBITaxon.
 *
 * Equality is simply the NCBI taxon ID.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public abstract class PersistentNCBITaxon extends SimpleNCBITaxon implements Persistent {
    
    private int status;
    private int uid;
    private PersistentBioDB db;
    
    protected PersistentNCBITaxon(PersistentBioDB db, int NCBITaxID) {
        super(NCBITaxID);
        this.status = Persistent.UNMODIFIED;
        this.uid = Persistent.UID_UNKNOWN;
        this.db = db;
    }
    
    protected PersistentNCBITaxon(PersistentBioDB db, NCBITaxon nt) {
        this(db,nt.getNCBITaxID());
        try {
            this.setNodeRank(nt.getNodeRank());
            this.setRightValue(nt.getRightValue());
            this.setLeftValue(nt.getLeftValue());
            this.setParentNCBITaxID(nt.getParentNCBITaxID());
            this.setMitoGeneticCode(nt.getMitoGeneticCode());
            this.setGeneticCode(nt.getGeneticCode());
            for (Iterator nc = nt.getNameClasses().iterator(); nc.hasNext(); ) {
                String nameClass = (String)nc.next();
                for (Iterator n = nt.getNames(nameClass).iterator(); n.hasNext(); ) {
                    String name = (String)n.next();
                    this.addName(nameClass, name);
                }
            }
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
    }
    
    public void setUid(int uid) {
        this.uid = uid;
    }
    
    public abstract Persistent load(Object[] vars) throws SQLException;
    
    public abstract boolean remove(Object[] vars) throws SQLException;
    
    public abstract Persistent store(Object[] vars) throws SQLException;
    
    public void setNodeRank(String nodeRank) throws ChangeVetoException {
        super.setNodeRank(nodeRank);
        this.status = Persistent.MODIFIED;
    }
    
    public void setRightValue(int rightValue) throws ChangeVetoException {
        super.setRightValue(rightValue);
        this.status = Persistent.MODIFIED;
    }
    
    public void setParentNCBITaxID(int parent) throws ChangeVetoException {
        super.setParentNCBITaxID(parent);
        this.status = Persistent.MODIFIED;
    }
    
    public void setMitoGeneticCode(int mitoGeneticCode) throws ChangeVetoException {
        super.setMitoGeneticCode(mitoGeneticCode);
        this.status = Persistent.MODIFIED;
    }
    
    public void setLeftValue(int leftValue) throws ChangeVetoException {
        super.setLeftValue(leftValue);
        this.status = Persistent.MODIFIED;
    }
    
    public void setGeneticCode(int geneticCode) throws ChangeVetoException {
        super.setGeneticCode(geneticCode);
        this.status = Persistent.MODIFIED;
    }
    
    // Contains all names removed since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Map removedNames = new HashMap();
    protected Map getRemovedNames() { return Collections.unmodifiableMap(this.removedNames); }
    protected void resetRemovedNames() { this.removedNames.clear(); }
    // Contains all names added since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Map addedNames = new HashMap();
    protected Map getAddedNames() { return Collections.unmodifiableMap(this.addedNames); }
    protected void resetAddedNames() { this.addedNames.clear(); }
    
    public boolean removeName(String nameClass, String name) throws IllegalArgumentException, ChangeVetoException {
        boolean retValue = super.removeName(nameClass, name);
        if (this.addedNames.containsKey(nameClass)) ((Set)this.addedNames.get(nameClass)).remove(name);
        synchronized(this.removedNames) {
            if (!(this.removedNames.containsKey(nameClass))) this.removedNames.put(nameClass,new HashSet());
        }
        ((Set)this.removedNames.get(nameClass)).add(name);
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
    public void addName(String nameClass, String name) throws IllegalArgumentException, ChangeVetoException {
        super.addName(nameClass, name);
        if (this.removedNames.containsKey(nameClass)) ((Set)this.removedNames.get(nameClass)).remove(name);
        synchronized(this.addedNames) {
            if (!(this.addedNames.containsKey(nameClass))) this.addedNames.put(nameClass,new HashSet());
        }
        ((Set)this.addedNames.get(nameClass)).add(name);
        this.status = Persistent.MODIFIED;
    }
    
}
