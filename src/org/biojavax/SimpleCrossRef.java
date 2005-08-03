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

import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeVetoException;

/**
 * A basic CrossRef implementation.
 * Equality is the dbname, accession and version combination.
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class SimpleCrossRef extends AbstractChangeable implements CrossRef {
    
    private RichAnnotation notes = new SimpleRichAnnotation();
    private String accession;
    private String dbname;
    private int version;
    
    /**
     * Creates a new instance of SimpleCrossRef
     * @param dbname the dbname for this crossref.
     * @param accession the accession for this crossref.
     * @param version the version for this crossref.
     */
    public SimpleCrossRef(String dbname, String accession, int version) {
        if (accession==null) throw new IllegalArgumentException("Accession cannot be null");
        if (dbname==null) throw new IllegalArgumentException("DBName cannot be null");
        this.accession = accession;
        this.dbname = dbname;
        this.version = version;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleCrossRef() {}
    
    /**
     * {@inheritDoc}
     */
    public Annotation getAnnotation() { return this.notes; }
    
    /**
     * {@inheritDoc}
     */
    public Set getNoteSet() { return this.notes.getNoteSet(); }
    
    /**
     * {@inheritDoc}
     */
    public void setNoteSet(Set notes) throws ChangeVetoException { this.notes.setNoteSet(notes); }
    
    /**
     * {@inheritDoc}
     */
    public String getAccession() { return this.accession; }
    
    // Hibernate requirement - not for public use.
    private void setAccession(String accession) { this.accession = accession; }
    
    /**
     * {@inheritDoc}
     */
    public String getDbname() { return this.dbname; }
    
    // Hibernate requirement - not for public use.
    private void setDbname(String dbname) { this.dbname = dbname; }
    
    /**
     * {@inheritDoc}
     */
    public int getVersion() { return this.version; }
    
    // Hibernate requirement - not for public use.
    private void setVersion(int version) { this.version = version; }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.dbname==null) return -1;
        // Normal comparison
        CrossRef them = (CrossRef)o;
        if (!this.dbname.equals(them.getDbname())) return this.dbname.compareTo(them.getDbname());
        if (!this.accession.equals(them.getAccession())) return this.accession.compareTo(them.getAccession());
        return this.version-them.getVersion();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj==null || !(obj instanceof CrossRef)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.dbname==null) return false;
        // Normal comparison
            CrossRef them = (CrossRef)obj;
            return (this.dbname.equals(them.getDbname()) &&
                    this.accession.equals(them.getAccession()) &&
                    this.version==them.getVersion()
                    );
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.dbname==null) return code;
        // Normal comparison
        code = 37*code + this.dbname.hashCode();
        code = 37*code + this.accession.hashCode();
        code = 37*code + this.version;
        return code;
    }
    
    
    /**
     * {@inheritDoc}
     * Form: <code>this.getDbname()+":"+this.getAccession()+", v."+this.getVersion();</code>
     */
    public String toString() {
        return this.getDbname()+":"+this.getAccession()+", v."+this.getVersion();
    }
    
// Hibernate requirement - not for public use.
    private Long id;
    
// Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
// Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
}

