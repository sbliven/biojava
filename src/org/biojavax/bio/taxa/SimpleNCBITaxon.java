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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.db.Persistent;

/**
 * Reference implementation of NCBITaxon.
 *
 * Equality is simply the NCBI taxon ID.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class SimpleNCBITaxon extends AbstractChangeable implements NCBITaxon {
    
    /**
     * The names for this taxon.
     */
    private Map names;
    /**
     * The parent for this taxon.
     */
    private int parent;
    /**
     * The NCBI Taxon ID for this taxon.
     */
    private int NCBITaxID;
    /**
     * The node rank for this taxon.
     */
    private String nodeRank;
    /**
     * The genetic code for this taxon.
     */
    private int geneticCode;
    /**
     * The mitogenetic code for this taxon.
     */
    private int mitoGeneticCode;
    /**
     * The left value for this taxon.
     */
    private int leftValue;
    /**
     * The right value for this taxon.
     */
    private int rightValue;
    
    /**
     * Creates a new instance of SimpleNCBITaxon
     * @param NCBITaxID the underlying taxon ID from NCBI.
     */
    public SimpleNCBITaxon(int NCBITaxID) {
        if (NCBITaxID==Persistent.NULL_INTEGER) throw new IllegalArgumentException("NCBI Taxon ID cannot be null");
        this.names = new HashMap();
        this.parent = Persistent.NULL_INTEGER;
        this.NCBITaxID = NCBITaxID;
        this.nodeRank = null;
        this.geneticCode = Persistent.NULL_INTEGER;
        this.mitoGeneticCode = Persistent.NULL_INTEGER;
        this.leftValue = Persistent.NULL_INTEGER;
        this.rightValue = Persistent.NULL_INTEGER;
    }
        
    /**
     * Returns all the name classes available for a taxon.
     * @return a set of name classes, or the empty set if there are none.
     */
    public Set getNameClasses() {
        return this.names.keySet();
    }
    
    /**
     * Returns all the names available for a taxon in a given class.
     * Class cannot be null.
     * @param nameClass the name class to retrieve names from.
     * @return a set of names, or the empty set if there are none.
     * @throws IllegalArgumentException if the name is null.
     */
    public Set getNames(String nameClass) throws IllegalArgumentException {
        if (nameClass==null) throw new IllegalArgumentException("Name class cannot be null");
        return Collections.unmodifiableSet((Set)this.names.get(nameClass));
    }
    
    /**
     * Adds the name to this taxon in the given name class. Neither can be null.
     * @param nameClass the class to add the name in.
     * @param name the name to add.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     * @throws IllegalArgumentException if the name is null.
     */
    public void addName(String nameClass, String name) throws IllegalArgumentException,ChangeVetoException {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        if (nameClass==null) throw new IllegalArgumentException("Name class cannot be null");
        if(!this.hasListeners(NCBITaxon.NAMES)) {
            if (!this.names.containsKey(nameClass)) this.names.put(nameClass,new HashSet());
            ((Set)this.names.get(nameClass)).add(name);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    NCBITaxon.NAMES,
                    name,
                    ((Set)this.names.get(nameClass)).contains(name)?name:null
                    );
            ChangeSupport cs = this.getChangeSupport(NCBITaxon.NAMES);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                if (!this.names.containsKey(nameClass)) this.names.put(nameClass,new HashSet());
                ((Set)this.names.get(nameClass)).add(name);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * Removes the name from the given name class. Neither can be null.
     * @return True if the name was found and removed, false otherwise.
     * @param nameClass the class to remove the name from.
     * @param name the name to remove.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     * @throws IllegalArgumentException if the name is null.
     */
    public boolean removeName(String nameClass, String name) throws IllegalArgumentException,ChangeVetoException {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        if (nameClass==null) throw new IllegalArgumentException("Name class cannot be null");
        if (!this.names.containsKey(nameClass)) return false;
        boolean results;
        if(!this.hasListeners(NCBITaxon.NAMES)) {
            results = ((Set)this.names.get(nameClass)).remove(name);
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
                results = ((Set)this.names.get(nameClass)).remove(name);
                cs.firePostChangeEvent(ce);
            }
        }
        return results;
    }
    
    /**
     * Tests for the presence of a name in a given class. Neither can be null.
     * @param nameClass the class to look the name up in.
     * @param name the name to text for existence of.
     * @return True if the name exists in that class, false otherwise.
     * @throws IllegalArgumentException if the name is null.
     */
    public boolean containsName(String nameClass, String name) throws IllegalArgumentException {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        if (nameClass==null) throw new IllegalArgumentException("Name class cannot be null");
        if (!this.names.containsKey(nameClass)) return false;
        return ((Set)this.names.get(nameClass)).contains(name);
    }
    
    /**
     * Getter for property parent.
     * @return Value of property parent.
     */
    public int getParentNCBITaxID() {
        return this.parent;
    }
    
    /**
     * Setter for property parent.
     * @param parent New value of property parent.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
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
     * Getter for property NCBITaxID.
     * @return Value of property NCBITaxID.
     */
    public int getNCBITaxID() {
        return this.NCBITaxID;
    }
    
    /**
     * Getter for property nodeRank.
     * @return Value of property nodeRank.
     */
    public String getNodeRank() {
        return this.nodeRank;
    }
    
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
     * Getter for property geneticCode. Returns Persistent.NULL_INTEGER if null.
     * @return Value of property geneticCode.
     */
    public int getGeneticCode() {
        return this.geneticCode;
    }
    
    /**
     * Setter for property geneticCode. Use Persistent.NULL_INTEGER for null.
     * @param geneticCode New value of property geneticCode.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
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
    public int getMitoGeneticCode() {
        return this.mitoGeneticCode;
    }
    
    /**
     * Setter for property mitoGeneticCode. Use Persistent.NULL_INTEGER for null.
     * @param mitoGeneticCode New value of property mitoGeneticCode.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
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
     * Getter for property leftValue. Returns Persistent.NULL_INTEGER if null.
     * @return Value of property leftValue.
     */
    public int getLeftValue() {
        return this.leftValue;
    }
    
    /**
     * Setter for property leftValue. Use Persistent.NULL_INTEGER for null.
     * @param leftValue New value of property leftValue.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
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
     * Getter for property rightValue. Returns Persistent.NULL_INTEGER if null.
     * @return Value of property rightValue.
     */
    public int getRightValue() {
        return this.rightValue;
    }
    
    /**
     * Setter for property rightValue. Use Persistent.NULL_INTEGER for null.
     * @param rightValue New value of property rightValue.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
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
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     * @return a negative integer, zero, or a positive integer as this object
     * 		is less than, equal to, or greater than the specified object.
     * @param o the Object to be compared.
     */
    public int compareTo(Object o) {
        NCBITaxon them = (NCBITaxon)o;
        return this.getNCBITaxID()-them.getNCBITaxID();
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see     #hashCode()
     * @see     java.util.Hashtable
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof NCBITaxon)) return false;
        NCBITaxon them = (NCBITaxon)obj;
        return this.getNCBITaxID()==them.getNCBITaxID();
    }
    
    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     * @return  a hash code value for this object.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.util.Hashtable
     */
    public int hashCode() {
        int code = 17;
        return 31*code + this.getNCBITaxID();
    }
    
    /**
     * Returns a string representation of the object of the form <code>
     * "taxid:"+this.getNCBITaxID();</code>
     * @return  a string representation of the object.
     */
    public String toString() {
        return "taxid:"+this.getNCBITaxID();
    }
    
}
