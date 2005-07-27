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
 
 * SimpleDocumentReference.java
 
 *
 
 * Created on June 15, 2005, 5:56 PM
 
 */



package org.biojavax;

import org.biojava.utils.AbstractChangeable;

import org.biojava.utils.ChangeEvent;

import org.biojava.utils.ChangeSupport;

import org.biojava.utils.ChangeVetoException;







/**
 *
 * A basic DocumentReference implementation.
 *
 *
 *
 * Equality is having a unique author and location.
 *
 *
 *
 * @author Richard Holland
 *
 * @author Mark Schreiber
 *
 */

public class SimpleDocumentReference extends AbstractChangeable implements DocumentReference {
    
    
    
    /**
     *
     * The crossref for this document reference.
     *
     */
    
    private CrossRef crossref;
    
    /**
     *
     * The authors for this document reference.
     *
     */
    
    private String authors;
    
    /**
     *
     * The title for this document reference.
     *
     */
    
    private String title;
    
    /**
     *
     * The location for this document reference.
     *
     */
    
    private String location;
    
    /**
     *
     * The crc for this document reference.
     *
     */
    
    private String crc;
    
    
    
    /**
     *
     * Creates a new document reference.
     *
     * @param authors The authors of the referenced document.
     *
     * @param location The location of the document, eg. the journal name and page range.
     *
     */
    
    public SimpleDocumentReference(String authors, String location) {
        
        if (authors==null) throw new IllegalArgumentException("Authors cannot be null");
        
        if (location==null) throw new IllegalArgumentException("Location cannot be null");
        
        this.crossref = null;
        
        this.authors = authors;
        
        this.title = null;
        
        this.location = location;
        
        this.crc = null;
        
    }
    
    // Hibernate requirement - not for public use.
    private SimpleDocumentReference() {}
    
    /**
     *
     * Setter for property CRC.
     *
     * @param CRC New value of property CRC.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setCRC(String CRC) throws ChangeVetoException {
        
        if(!this.hasListeners(DocumentReference.CRC)) {
            
            this.crc = CRC;
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    DocumentReference.CRC,
                    
                    CRC,
                    
                    this.crc
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(DocumentReference.CRC);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.crc = CRC;
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Setter for property title.
     *
     * @param title New value of property title.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setTitle(String title) throws ChangeVetoException {
        
        if(!this.hasListeners(DocumentReference.TITLE)) {
            
            this.title = title;
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    DocumentReference.TITLE,
                    
                    title,
                    
                    this.title
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(DocumentReference.TITLE);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.title = title;
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Setter for property crossref.
     *
     * @param crossref New value of property crossref.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setCrossref(CrossRef crossref) throws ChangeVetoException {
        
        if(!this.hasListeners(DocumentReference.CROSSREF)) {
            
            this.crossref = crossref;
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    DocumentReference.CROSSREF,
                    
                    crossref,
                    
                    this.crossref
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(DocumentReference.CROSSREF);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.crossref = crossref;
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    // Hibernate requirement - not for public use.
    private void setAuthors(String authors) { this.authors = authors; }
    
    // Hibernate requirement - not for public use.
    private void setLocation(String location) { this.location = location; }
    
    /**
     *
     * Getter for property authors.
     *
     * @return Value of property authors.
     *
     */
    
    public String getAuthors() {
        
        return this.authors;
        
    }
    
    
    
    /**
     *
     * Getter for property CRC.
     *
     * @return Value of property CRC.
     *
     */
    
    public String getCRC() {
        
        return this.crc;
        
    }
    
    
    
    /**
     *
     * Getter for property crossref.
     *
     * @return Value of property crossref.
     *
     */
    
    public CrossRef getCrossref() {
        
        return this.crossref;
        
    }
    
    
    
    /**
     *
     * Getter for property location.
     *
     * @return Value of property location.
     *
     */
    
    public String getLocation() {
        
        return this.location;
        
    }
    
    
    
    /**
     *
     * Getter for property title.
     *
     * @return Value of property title.
     *
     */
    
    public String getTitle() {
        
        return this.title;
        
    }
    
    
    
    /**
     *
     * Compares this object with the specified object for order.  Returns a
     *
     * negative integer, zero, or a positive integer as this object is less
     *
     * than, equal to, or greater than the specified object.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *
     * 		is less than, equal to, or greater than the specified object.
     *
     * @param o the Object to be compared.
     *
     */
    
    public int compareTo(Object o) {
        
        DocumentReference them = (DocumentReference)o;
        
        if (!this.getAuthors().equals(them.getAuthors())) return this.getAuthors().compareTo(them.getAuthors());
        
        return this.getLocation().compareTo(them.getLocation());
        
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
        
        if (obj==null || !(obj instanceof DocumentReference)) return false;
        
        else {
            
            DocumentReference them = (DocumentReference)obj;
            
            return (this.getAuthors().equals(them.getAuthors()) &&
                    
                    this.getLocation().equals(them.getLocation()));
            
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
        
        code = 37*code + this.getAuthors().hashCode();
        
        code = 37*code + this.getLocation().hashCode();
        
        return code;
        
    }
    
    
    
    /**
     *
     * Returns a string representation of the object of the form <code>
     *
     * this.getAuthors()+"; "+this.getLocation();</code>
     *
     * @return  a string representation of the object.
     *
     */
    
    public String toString() {
        
        return this.getAuthors()+"; "+this.getLocation();
        
    }
    
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    
    // Hibernate requirement - not for public use.
    private Long getId() {
        
        return this.id;
    }
    
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) {
        
        this.id = id;
    }
    
}

