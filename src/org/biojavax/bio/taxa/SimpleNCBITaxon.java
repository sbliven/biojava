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
 * SimpleNCBITaxon.java
 *
 * Created on June 16, 2005, 10:01 AM
 */

package org.biojavax.bio.taxa;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;

/**
 * Reference implementation of NCBITaxon.
 * Equality is simply the NCBI taxon ID.
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class SimpleNCBITaxon extends AbstractChangeable implements NCBITaxon {
    
    private Map names = new HashMap();
    private int parent;
    private int NCBITaxID;
    private String nodeRank;
    private int geneticCode;
    private int mitoGeneticCode;
    private int leftValue;
    private int rightValue;
    
    /**
     * Creates a new instance of SimpleNCBITaxon
     * @param NCBITaxID the underlying taxon ID from NCBI.
     */
    public SimpleNCBITaxon(int NCBITaxID) {
        this.parent = 0;
        this.NCBITaxID = NCBITaxID;
        this.nodeRank = null;
        this.geneticCode = 0;
        this.mitoGeneticCode = 0;
        this.leftValue = 0;
        this.rightValue = 0;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleNCBITaxon() {}
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        NCBITaxon them = (NCBITaxon)o;
        return this.getNCBITaxID()-them.getNCBITaxID();
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof NCBITaxon)) return false;
        NCBITaxon them = (NCBITaxon)obj;
        return this.getNCBITaxID()==them.getNCBITaxID();
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        return 31*code + this.getNCBITaxID();
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getNameClasses() { return this.names.keySet(); }
    
    // Hibernate requirement - not for public use.
    private class SimpleNCBITaxonName {
        private String nameClass;
        private String name;
        private NCBITaxon parent;
        public SimpleNCBITaxonName() {}
        public SimpleNCBITaxonName(NCBITaxon parent, String nameClass, String name) {
            this.parent = parent;
            this.nameClass = nameClass;
            this.name = name; }
        public void setNameClass(String nameClass) { this.nameClass = nameClass; }
        public String getNameClass() { return this.nameClass; }
        public void setName(String name) { this.name = name; }
        public String getName() { return this.name; }
        public void setParent(NCBITaxon parent) { this.parent = parent; }
        public NCBITaxon getParent() { return this.parent; }
        public boolean equals(Object o) {
            if (o==this) return true;
            if (!(o instanceof SimpleNCBITaxonName)) return false;
            SimpleNCBITaxonName them = (SimpleNCBITaxonName) o;
            return them.getNameClass().equals(this.nameClass) &&
                    them.getName().equals(this.name) &&
                    them.getParent().equals(this.parent);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getNames(String nameClass) throws IllegalArgumentException {
        if (nameClass==null) throw new IllegalArgumentException("Name class cannot be null");
        Set n = new HashSet();
        for (Iterator j = ((Set)this.names.get(nameClass)).iterator(); j.hasNext(); ) {
            SimpleNCBITaxonName name = (SimpleNCBITaxonName)j.next();
            n.add(name.getName());
        }
        return n;
    }
    
    // Hibernate requirement - not for public use.
    private Set getNameSet() {
        Set n = new HashSet();
        for (Iterator i = this.names.values().iterator(); i.hasNext(); ) n.addAll((Set)i.next());
        return n;
    }
    
    // Hibernate requirement - not for public use.
    private void setNameSet(Set names) {
        this.names.clear();
        for (Iterator i = names.iterator(); i.hasNext(); ) {
            SimpleNCBITaxonName n = (SimpleNCBITaxonName)i.next();
            try {
                this.addName(n.getNameClass(), n.getName());
            } catch (ChangeVetoException e) {
                throw new RuntimeException("Database contents don't add up",e);
            }
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void addName(String nameClass, String name) throws IllegalArgumentException,ChangeVetoException {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        if (nameClass==null) throw new IllegalArgumentException("Name class cannot be null");
        SimpleNCBITaxonName n = new SimpleNCBITaxonName(this, nameClass, name);
        if(!this.hasListeners(NCBITaxon.NAMES)) {
            if (!this.names.containsKey(nameClass)) this.names.put(nameClass,new HashSet());
            ((Set)this.names.get(nameClass)).add(n);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    NCBITaxon.NAMES,
                    name,
                    ((Set)this.names.get(nameClass)).contains(n)?name:null
                    );
            ChangeSupport cs = this.getChangeSupport(NCBITaxon.NAMES);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                if (!this.names.containsKey(nameClass)) this.names.put(nameClass,new HashSet());
                ((Set)this.names.get(nameClass)).add(n);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean removeName(String nameClass, String name) throws IllegalArgumentException,ChangeVetoException {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        if (nameClass==null) throw new IllegalArgumentException("Name class cannot be null");
        SimpleNCBITaxonName n = new SimpleNCBITaxonName(this, nameClass, name);
        if (!this.names.containsKey(nameClass)) return false;
        boolean results;
        if(!this.hasListeners(NCBITaxon.NAMES)) {
            results = ((Set)this.names.get(nameClass)).remove(n);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    NCBITaxon.NAMES,
                    null,
                    name
                    );
            ChangeSupport cs = this.getChangeSupport(NCBITaxon.NAMES);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                results = ((Set)this.names.get(nameClass)).remove(n);
                cs.firePostChangeEvent(ce);
            }
        }
        return results;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsName(String nameClass, String name) throws IllegalArgumentException {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        if (nameClass==null) throw new IllegalArgumentException("Name class cannot be null");
        if (!this.names.containsKey(nameClass)) return false;
        SimpleNCBITaxonName n = new SimpleNCBITaxonName(this, nameClass, name);
        return ((Set)this.names.get(nameClass)).contains(n);
    }
    
    /**
     * {@inheritDoc}
     */
    public int getParentNCBITaxID() { return this.parent; }
    
    /**
     * {@inheritDoc}
     */
    public void setParentNCBITaxID(int parent) throws ChangeVetoException {
        if(!this.hasListeners(NCBITaxon.PARENT)) {
            this.parent = parent;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    NCBITaxon.PARENT,
                    Integer.valueOf(parent),
                    Integer.valueOf(this.parent)
                    );
            ChangeSupport cs = this.getChangeSupport(NCBITaxon.PARENT);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.parent = parent;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getNCBITaxID() { return this.NCBITaxID; }
    
    // Hibernate requirement - not for public use.
    private void setNCBITaxID(int NCBITaxID) { this.NCBITaxID = NCBITaxID; }
    
    /**
     * {@inheritDoc}
     */
    public String getNodeRank() { return this.nodeRank; }
    
    /**
     * Setter for property nodeRank.
     * @param nodeRank New value of property nodeRank.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setNodeRank(String nodeRank) throws ChangeVetoException {
        if(!this.hasListeners(NCBITaxon.NODERANK)) {
            this.nodeRank = nodeRank;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    NCBITaxon.NODERANK,
                    nodeRank,
                    this.nodeRank
                    );
            ChangeSupport cs = this.getChangeSupport(NCBITaxon.NODERANK);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.nodeRank = nodeRank;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getGeneticCode() { return this.geneticCode; }
    
    /**
     * {@inheritDoc}
     */
    public void setGeneticCode(int geneticCode) throws ChangeVetoException {
        if(!this.hasListeners(NCBITaxon.GENETICCODE)) {
            this.geneticCode = geneticCode;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    NCBITaxon.GENETICCODE,
                    new Integer(nodeRank),
                    new Integer(this.nodeRank)
                    );
            ChangeSupport cs = this.getChangeSupport(NCBITaxon.GENETICCODE);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.geneticCode = geneticCode;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * Getter for property mitoGeneticCode. Returns Persistent.NULL_INTEGER if null.
     * @return Value of property mitoGeneticCode.
     */
    public int getMitoGeneticCode() { return this.mitoGeneticCode; }
    
    /**
     * {@inheritDoc}
     */
    public void setMitoGeneticCode(int mitoGeneticCode) throws ChangeVetoException {
        if(!this.hasListeners(NCBITaxon.MITOGENETICCODE)) {
            this.mitoGeneticCode = mitoGeneticCode;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    NCBITaxon.MITOGENETICCODE,
                    new Integer(mitoGeneticCode),
                    new Integer(this.mitoGeneticCode)
                    );
            ChangeSupport cs = this.getChangeSupport(NCBITaxon.MITOGENETICCODE);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.mitoGeneticCode = mitoGeneticCode;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getLeftValue() { return this.leftValue; }
    
    /**
     * {@inheritDoc}
     */
    public void setLeftValue(int leftValue) throws ChangeVetoException {
        if(!this.hasListeners(NCBITaxon.LEFTVALUE)) {
            this.leftValue = leftValue;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    NCBITaxon.LEFTVALUE,
                    new Integer(leftValue),
                    new Integer(this.leftValue)
                    );
            ChangeSupport cs = this.getChangeSupport(NCBITaxon.LEFTVALUE);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.leftValue = leftValue;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getRightValue() { return this.rightValue; }
    
    /**
     * {@inheritDoc}
     */
    public void setRightValue(int rightValue) throws ChangeVetoException {
        if(!this.hasListeners(NCBITaxon.RIGHTVALUE)) {
            this.rightValue = rightValue;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    NCBITaxon.RIGHTVALUE,
                    new Integer(rightValue),
                    new Integer(this.rightValue)
                    );
            ChangeSupport cs = this.getChangeSupport(NCBITaxon.RIGHTVALUE);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.rightValue = rightValue;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * In the form <code>"taxid:"+this.getNCBITaxID();</code>
     */
    public String toString() { return "taxid:"+this.getNCBITaxID(); }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
    
}

