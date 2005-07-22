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
 
 * SimpleCrossRef.java
 
 *
 
 * Created on June 15, 2005, 5:32 PM
 
 */



package org.biojavax;

import java.util.Collections;

import java.util.List;

import java.util.Vector;

import org.biojava.ontology.AlreadyExistsException;

import org.biojava.utils.AbstractChangeable;

import org.biojava.utils.ChangeEvent;

import org.biojava.utils.ChangeSupport;

import org.biojava.utils.ChangeVetoException;

import org.biojavax.bio.db.Persistent;

import org.biojavax.ontology.ComparableTerm;



/**
 *
 * A basic CrossRef implementation.
 *
 *
 *
 * Equality is the dbname, accession and version combination.
 *
 *
 *
 * @author Richard Holland
 *
 * @author Mark Schreiber
 *
 */

public class SimpleCrossRef extends AbstractChangeable implements CrossRef {
    
    
    
    /**
     *
     * The terms associated with this cross reference.
     *
     */
    
    private Vector terms;
    private Vector values;
    
    /**
     *
     * The accession for this cross reference.
     *
     */
    
    private String accession;
    
    /**
     *
     * The dbname for this cross reference.
     *
     */
    
    private String dbname;
    
    /**
     *
     * The version for this cross reference.
     *
     */
    
    private int version;
    
    
    
    /**
     *
     * Creates a new instance of SimpleCrossRef
     *
     * @param dbname the dbname for this crossref.
     *
     * @param accession the accession for this crossref.
     *
     * @param version the version for this crossref.
     *
     */
    
    public SimpleCrossRef(String dbname, String accession, int version) {
        
        if (accession==null) throw new IllegalArgumentException("Accession cannot be null");
        
        if (dbname==null) throw new IllegalArgumentException("DBName cannot be null");
        
        if (version==Persistent.NULL_INTEGER) throw new IllegalArgumentException("Version cannot be null");
        
        this.accession = accession;
        
        this.dbname = dbname;
        
        this.version = version;
        
        this.terms = new Vector();
        
        this.values = new Vector();
        
    }
    
    
    
    /**
     *
     * Returns the term at a given index. If the index is valid but no term is
     *
     * found at that position, it will return null. If the index is invalid,
     *
     * an exception will be thrown.
     *
     * @return The term at that index position.
     *
     * @param index the index of the term to retrieve.
     *
     */
    
    public ComparableTerm getTerm(int index) {
        
        return (ComparableTerm)this.terms.get(index);
        
    }
    
    public String getTermValue(int index) {
        
        return (String)this.values.get(index);
        
    }
    
    
    
    /**
     *
     * Removes the term at a given index. If the index position already had no
     *
     * term associated, it returns false. Else, it returns true.
     *
     * @return True if a term was found at that position and removed.
     *
     * @param index the index position to remove the term from.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public boolean removeTerm(int index) throws ChangeVetoException{
        
        if (this.terms.get(index)==null) return false;
        
        else {
            
            if(!this.hasListeners(CrossRef.TERM)) {
                
                this.terms.set(index,null);
                
                this.values.set(index,null);
                
            } else {
                
                ChangeEvent ce = new ChangeEvent(
                        
                        this,
                        
                        CrossRef.TERM,
                        
                        null,
                        
                        this.terms.get(index)
                        
                        );
                
                ChangeSupport cs = this.getChangeSupport(CrossRef.TERM);
                
                synchronized(cs) {
                    
                    cs.firePreChangeEvent(ce);
                    
                    this.terms.set(index,null);
                    
                    this.values.set(index,null);
                    
                    cs.firePostChangeEvent(ce);
                    
                }
                
            }
            
            return true;
            
        }
        
    }
    
    
    
    /**
     *
     * Overwrites the list of terms at the given index position with the term
     *
     * supplied. It will overwrite anything already at that position.
     *
     * @param term New term to write at that position.
     *
     * @param index Position to write term at.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws AlreadyExistsException if the term already exists at another index.
     *
     * @throws IllegalArgumentException in case of missing term.
     *
     */
    
    public void setTerm(ComparableTerm term, String value, int index) throws AlreadyExistsException,IllegalArgumentException,ChangeVetoException {
        
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        
        if (this.terms.contains(term)) throw new AlreadyExistsException("Term has already been added");
        
        if(!this.hasListeners(CrossRef.TERM)) {
            
            this.terms.ensureCapacity(index+1);
            
            this.terms.set(index,term);
            
            this.values.ensureCapacity(index+1);
            
            this.values.set(index,value);
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    CrossRef.TERM,
                    
                    term,
                    
                    null
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(CrossRef.TERM);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.terms.ensureCapacity(index+1);
                
                this.terms.set(index,term);
                
                this.values.ensureCapacity(index+1);
                
                this.values.set(index,value);
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Adds the term to the end of the list of terms, giving it the index of
     *
     * max(all other term index positions)+1.
     *
     * @return The position the term was added at.
     *
     * @param term New term to add.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws AlreadyExistsException if the term already exists at another index.
     *
     * @throws IllegalArgumentException in case of missing term.
     *
     */
    
    public int addTerm(ComparableTerm term, String value) throws AlreadyExistsException,IllegalArgumentException,ChangeVetoException {
        
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        
        if (this.terms.contains(term)) throw new AlreadyExistsException("Term has already been added");
        
        int index = this.terms.size();
        
        if(!this.hasListeners(CrossRef.TERM)) {
            
            this.terms.ensureCapacity(index+1);
            
            this.terms.add(index,term);
            
            this.values.ensureCapacity(index+1);
            
            this.values.set(index,value);
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    CrossRef.TERM,
                    
                    term,
                    
                    null
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(CrossRef.TERM);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.terms.ensureCapacity(index+1);
                
                this.terms.add(index,term);
                
                this.values.ensureCapacity(index+1);
                
                this.values.set(index,value);
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
        return index;
        
    }
    
    
    
    /**
     *
     *
     *
     * Tests for the existence of a term in the list.
     *
     * @return True if the term is in the list, false if not.
     *
     * @param term the term to look for.
     *
     */
    
    public boolean containsTerm(ComparableTerm term) {
        
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        
        return this.terms.contains(term);
        
    }
    
    
    
    /**
     *
     * Searches for a term in the list of all terms, and removes it if it was
     *
     * found.
     *
     * @return True if the term was found, false if the term was not found.
     *
     * @param term the term to search for and remove.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public boolean removeTerm(ComparableTerm term) throws ChangeVetoException {
        
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        
        int index = this.terms.indexOf(term);
        
        if (index>=0) {
            
            if(!this.hasListeners(CrossRef.TERM)) {
                
                this.terms.set(index,null);
                
                this.values.set(index,null);
                
            } else {
                
                ChangeEvent ce = new ChangeEvent(
                        
                        this,
                        
                        CrossRef.TERM,
                        
                        null,
                        
                        this.terms.get(index)
                        
                        );
                
                ChangeSupport cs = this.getChangeSupport(CrossRef.TERM);
                
                synchronized(cs) {
                    
                    cs.firePreChangeEvent(ce);
                    
                    this.terms.set(index,null);
                    
                    this.values.set(index,null);
                    
                    cs.firePostChangeEvent(ce);
                    
                }
                
            }
            
            return true;
            
        } else {
            
            return false;
            
        }
        
    }
    
    
    
    /**
     *
     * Getter for property accession.
     *
     * @return Value of property accession.
     *
     */
    
    public String getAccession() {
        
        return this.accession;
        
    }
    
    
    
    /**
     *
     * Getter for property dbname.
     *
     * @return Value of property dbname.
     *
     */
    
    public String getDbname() {
        
        return this.dbname;
        
    }
    
    
    
    /**
     *
     * Getter for property version.
     *
     * @return Value of property version.
     *
     */
    
    public int getVersion() {
        
        return this.version;
        
    }
    
    
    
    /**
     *
     * Returns a list of all terms associated with this cross reference. This
     *
     * list is not mutable. If no terms are associated, you will get back an
     *
     * empty list. If the terms have indexes that are not consecutive, then the
     *
     * list will contain nulls at the indexes corresponding to the gaps between
     *
     * the extant terms. eg. If there are only two terms A and B at positions 10
     *
     * and 20 respectively, then the List returned will be of size 20, with nulls
     *
     * at index positions 0-9 and 11-19.
     *
     * @return Value of property terms.
     *
     */
    
    public List getTerms() {
        
        return Collections.unmodifiableList(this.terms);
        
    }
    
    
    
    /**
     *
     * Indicates whether some other object is "equal to" this one.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *
     * 		is less than, equal to, or greater than the specified object.
     *
     * @param o the Object to be compared.
     *
     */
    
    public int compareTo(Object o) {
        
        CrossRef them = (CrossRef)o;
        
        if (!this.getDbname().equals(them.getDbname())) return this.getDbname().compareTo(them.getDbname());
        
        if (!this.getAccession().equals(them.getAccession())) return this.getAccession().compareTo(them.getAccession());
        
        return this.getVersion()-them.getVersion();
        
    }
    
    
    
    /**
     *
     * Indicates whether some other object is "equal to" this one.
     *
     * @param   obj   the reference object with which to compare.
     *
     * @return  <code>true</code> if this object is the same as the obj
     *
     *          argument; <code>false</code> otherwise.
     *
     * @see     #hashCode()
     *
     * @see     java.util.Hashtable
     *
     */
    
    public boolean equals(Object obj) {
        
        if(this == obj) return true;
        
        if (obj==null || !(obj instanceof CrossRef)) return false;
        
        else {
            
            CrossRef them = (CrossRef)obj;
            
            return (this.getDbname().equals(them.getDbname()) &&
                    
                    this.getAccession().equals(them.getAccession()) &&
                    
                    this.getVersion()==them.getVersion()
                    
                    );
            
        }
        
    }
    
    
    
    /**
     *
     * Returns a hash code value for the object. This method is
     *
     * supported for the benefit of hashtables such as those provided by
     *
     * <code>java.util.Hashtable</code>.
     *
     * @return  a hash code value for this object.
     *
     * @see     java.lang.Object#equals(java.lang.Object)
     *
     * @see     java.util.Hashtable
     *
     */
    
    public int hashCode() {
        
        int code = 17;
        
        code = 37*code + this.getDbname().hashCode();
        
        code = 37*code + this.getAccession().hashCode();
        
        code = 37*code + this.getVersion();
        
        return code;
        
    }
    
    
    
    /**
     *
     * Returns a string representation of the object of the form <code>
     *
     * this.getDbname()+":"+this.getAccession()+", v."+this.getVersion();</code>
     *
     * @return  a string representation of the object.
     *
     */
    
    public String toString() {
        
        return this.getDbname()+":"+this.getAccession()+", v."+this.getVersion();
        
    }
    
}

