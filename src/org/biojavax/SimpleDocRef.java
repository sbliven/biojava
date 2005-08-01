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
 * SimpleDocRef.java
 *
 * Created on June 15, 2005, 5:56 PM
 */

package org.biojavax;

import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;

/**
 * A basic DocRef implementation.
 * Equality is having a unique author and location.
 * @author Richard Holland
 * @author Mark Schreiber
 */

public class SimpleDocRef extends AbstractChangeable implements DocRef {
    
    private CrossRef crossref;
    private String authors;
    private String title;
    private String location;
    private String crc;
    
    /**
     * Creates a new document reference.
     * @param authors The authors of the referenced document.
     * @param location The location of the document, eg. the journal name and page range.
     */
    public SimpleDocRef(String authors, String location) {
        if (authors==null) throw new IllegalArgumentException("Authors cannot be null");
        if (location==null) throw new IllegalArgumentException("Location cannot be null");
        this.crossref = null;
        this.authors = authors;
        this.title = null;
        this.location = location;
        this.crc = null;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleDocRef() {}
    
    /**
     * {@inheritDocs}
     */
    public void setCRC(String CRC) throws ChangeVetoException {
        if(!this.hasListeners(DocRef.CRC)) {
            this.crc = CRC;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    DocRef.CRC,
                    CRC,
                    this.crc
                    );
            ChangeSupport cs = this.getChangeSupport(DocRef.CRC);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.crc = CRC;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDocs}
     */
    public void setTitle(String title) throws ChangeVetoException {
        if(!this.hasListeners(DocRef.TITLE)) {
            this.title = title;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    DocRef.TITLE,
                    title,
                    this.title
                    );
            ChangeSupport cs = this.getChangeSupport(DocRef.TITLE);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.title = title;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDocs}
     */
    public void setCrossref(CrossRef crossref) throws ChangeVetoException {
        if(!this.hasListeners(DocRef.CROSSREF)) {
            this.crossref = crossref;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    DocRef.CROSSREF,
                    crossref,
                    this.crossref
                    );
            ChangeSupport cs = this.getChangeSupport(DocRef.CROSSREF);
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
     * {@inheritDocs}
     */
    public String getAuthors() { return this.authors; }
    
    /**
     * {@inheritDocs}
     */
    public String getCRC() { return this.crc; }
    
    /**
     * {@inheritDocs}
     */
    public CrossRef getCrossref() { return this.crossref; }
    
    /**
     * {@inheritDocs}
     */
    public String getLocation() { return this.location; }
    
    /**
     * {@inheritDocs}
     */
    public String getTitle() { return this.title; }
    
    /**
     * {@inheritDocs}
     */
    public int compareTo(Object o) {
        DocRef them = (DocRef)o;
        if (!this.getAuthors().equals(them.getAuthors())) return this.getAuthors().compareTo(them.getAuthors());
        return this.getLocation().compareTo(them.getLocation());
    }
    
    /**
     * {@inheritDocs}
     */
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj==null || !(obj instanceof DocRef)) return false;
        else {
            DocRef them = (DocRef)obj;
            return (this.getAuthors().equals(them.getAuthors()) &&
                    this.getLocation().equals(them.getLocation()));
        }
    }
    
    /**
     * {@inheritDocs}
     */
    public int hashCode() {
        int code = 17;
        code = 37*code + this.getAuthors().hashCode();
        code = 37*code + this.getLocation().hashCode();
        return code;
    }
    
    /**
     * {@inheritDocs}
     * Form: this.getAuthors()+"; "+this.getLocation();</code>
     */
    public String toString() { return this.getAuthors()+"; "+this.getLocation(); }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }    
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
    
}

