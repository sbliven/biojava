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

package org.biojavax;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Checksum;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.utils.CRC64Checksum;

/**
 * A basic DocRef implementation.
 * @author Richard Holland
 * @author Mark Schreiber
 */

public class SimpleDocRef extends AbstractChangeable implements DocRef {
    
    private CrossRef crossref;
    private List authors;
    private String title;
    private String location;
    private String remark;
    
    /**
     * Creates a new document reference from the given immutable authors and
     * location. Will throw exceptions if either are null.
     * @param authors The authors of the referenced document, as a set of DocRefAuthor instances.
     * @param location The location of the document, eg. the journal name and page range.
     */
    public SimpleDocRef(List authors, String location) {
        if (authors==null || authors.isEmpty()) throw new IllegalArgumentException("Authors cannot be null or empty");
        if (location==null) throw new IllegalArgumentException("Location cannot be null");
        this.crossref = null;
        this.authors = new ArrayList();
        this.authors.addAll(authors);
        this.title = null;
        this.location = location;
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
    
    // Hibernate requirement - not for public use.
    private void setCRC(String CRC) {} // ignore as field is a calculated value
    
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
    private void setAuthors(String authors) { this.authors = DocRefAuthor.Tools.parseAuthorString(authors); }
    
    // Hibernate requirement - not for public use.
    private void setLocation(String location) { this.location = location; }
    
    /**
     * {@inheritDoc}
     */
    public String getAuthors() { return DocRefAuthor.Tools.generateAuthorString(this.authors); }
    
    /**
     * {@inheritDoc}
     */
    public List getAuthorList() { return new ArrayList(this.authors); }
    
    /**
     * {@inheritDoc}
     * The string to be checksummed is constructed by concatenating the authors,
     * title, and location in that order, with no space between. If any values are
     * null they are substituted with the text "&lt;undef>".
     * @see CRC64Checksum
     */
    public String getCRC() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getAuthors());
        sb.append((this.title==null || this.title.equals(""))?"<undef>":this.title);
        sb.append((this.location==null || this.location.equals(""))?"<undef>":this.location);
        Checksum cs = new CRC64Checksum();
        cs.update(sb.toString().getBytes(), 0, sb.length());
        return cs.toString();
    }
    
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
     * Document references are compared first by author, then by location.
     */
    public int compareTo(Object o) {
        if(o == this) return 0;
        // Hibernate comparison - we haven't been populated yet
        if (this.authors==null) return -1;
        // Normal comparison
        DocRef them = (DocRef)o;
        if (!this.getAuthors().equals(them.getAuthors())) return this.getAuthors().compareTo(them.getAuthors());
        return this.location.compareTo(them.getLocation());
    }
    
    /**
     * {@inheritDoc}
     * Document references are equal if they have the same author and location.
     */
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj==null || !(obj instanceof DocRef)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.authors==null) return false;
        // Normal comparison
        DocRef them = (DocRef)obj;
        return (this.getAuthors().equals(them.getAuthors()) &&
                this.getLocation().equals(them.getLocation()));
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.authors==null) return code;
        // Normal comparison
        code = 37*code + this.getAuthors().hashCode();
        code = 37*code + this.location.hashCode();
        return code;
    }
    
    /**
     * {@inheritDoc}
     * Form: "authors; location"
     */
    public String toString() {
        return this.getAuthors()+"; "+this.getLocation();
    }
    
    // Hibernate requirement - not for public use.
    private Integer id;
    
    // Hibernate requirement - not for public use.
    private Integer getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Integer id) { this.id = id; }
    
}

