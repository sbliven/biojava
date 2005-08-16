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
    private String remark;
    
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
        this.remark = null;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleDocRef() {}
    
    /**
     * {@inheritDoc}
     */
    public void setRemark(String remark) throws ChangeVetoException {
        if(!this.hasListeners(DocRef.REMARK)) {
            this.remark = remark;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    DocRef.REMARK,
                    remark,
                    this.remark
                    );
            ChangeSupport cs = this.getChangeSupport(DocRef.REMARK);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.remark = remark;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public String getAuthors() { return this.authors; }
    
    /**
     * {@inheritDoc}
     */
    public String getCRC() { return this.crc; }
    
    /**
     * {@inheritDoc}
     */
    public String getRemark() { return this.remark; }
    
    /**
     * {@inheritDoc}
     */
    public CrossRef getCrossref() { return this.crossref; }
    
    /**
     * {@inheritDoc}
     */
    public String getLocation() { return this.location; }
    
    /**
     * {@inheritDoc}
     */
    public String getTitle() { return this.title; }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.authors==null) return -1;
        // Normal comparison
        DocRef them = (DocRef)o;
        if (!this.authors.equals(them.getAuthors())) return this.authors.compareTo(them.getAuthors());
        return this.location.compareTo(them.getLocation());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj==null || !(obj instanceof DocRef)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.authors==null) return false;
        // Normal comparison
        DocRef them = (DocRef)obj;
        return (this.authors.equals(them.getAuthors()) &&
                this.location.equals(them.getLocation()));
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.authors==null) return code;
        // Normal comparison
        code = 37*code + this.authors.hashCode();
        code = 37*code + this.location.hashCode();
        return code;
    }
    
    /**
     * {@inheritDoc}
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

