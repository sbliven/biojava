/*
 * BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence. This should
 * be distributed with the code. If you do not have a copy,
 * see:
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors. These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 * http://www.biojava.org/
 *
 */

/*
 * SimpleNamespace.java
 *
 * Created on June 15, 2005, 6:04 PM
 */

package org.biojavax;

import java.net.URI;
import java.net.URISyntaxException;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;

/**
 * A basic Namespace implemenation.
 * Equality is based on the name of the namespace.
 * @author Richard Holland
 * @author Mark Schreiber
 */

public class SimpleNamespace extends AbstractChangeable implements Namespace {
    
    private String name;
    private String acronym;
    private String authority;
    private String description;
    private URI URI;
    
    /**
     * Creates a new instance of SimpleNamespace
     * @param name the name of the namespace.
     */
    public SimpleNamespace(String name) {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        this.name = name;
        this.acronym = null;
        this.authority = null;
        this.description = null;
        this.URI = null;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleNamespace() {}
    
    /**
     * {@inheritDocs}
     */
    public void setAcronym(String acronym) throws ChangeVetoException {
        if(!this.hasListeners(Namespace.ACRONYM)) {
            this.acronym = acronym;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    Namespace.ACRONYM,
                    acronym,
                    this.acronym
                    );
            ChangeSupport cs = this.getChangeSupport(Namespace.ACRONYM);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.acronym = acronym;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDocs}
     */
    public void setAuthority(String authority) throws ChangeVetoException {
        if(!this.hasListeners(Namespace.AUTHORITY)) {
            this.authority = authority; } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    Namespace.AUTHORITY,
                    authority,
                    this.authority
                    );
            ChangeSupport cs = this.getChangeSupport(Namespace.AUTHORITY);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.authority = authority;
                cs.firePostChangeEvent(ce);
            }
            }
    }
    
    /**
     * {@inheritDocs}
     */
    public void setDescription(String description) throws ChangeVetoException {
        if(!this.hasListeners(Namespace.DESCRIPTION)) {
            this.description = description;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    Namespace.DESCRIPTION,
                    description,
                    this.description
                    );
            ChangeSupport cs = this.getChangeSupport(Namespace.DESCRIPTION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.description = description;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    // Hibernate requirement - not for public use.
    private void setURIString(String URI) throws ChangeVetoException, URISyntaxException { this.setURI(new URI(URI)); }
    
    // Hibernate requirement - not for public use.
    private String getURIString() { return this.URI.toASCIIString(); }
    
    /**
     * {@inheritDocs}
     */
    public void setURI(URI URI) throws ChangeVetoException {
        if(!this.hasListeners(Namespace.URI)) {
            this.URI = URI;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    Namespace.URI,
                    URI,
                    this.URI
                    );
            ChangeSupport cs = this.getChangeSupport(Namespace.URI);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.URI = URI;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    // Hibernate requirement - not for public use.
    private void setName(String name) { this.name = name; }
    
    /**
     * {@inheritDocs}
     */
    public String getAcronym() { return this.acronym; }
    
    /**
     * {@inheritDocs}
     */
    public String getAuthority() { return this.authority; }
    
    /**
     * {@inheritDocs}
     */
    public String getDescription() { return this.description; }
    
    /**
     * {@inheritDocs}
     */
    public String getName() { return this.name; }
    
    /**
     * {@inheritDocs}
     */
    public URI getURI() { return this.URI; }
    
    /**
     * {@inheritDocs}
     */
    public int compareTo(Object o) {
        Namespace them = (Namespace)o;
        return this.getName().compareTo(them.getName());
    }
    
    /**
     * {@inheritDocs}
     */
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj==null || !(obj instanceof Namespace)) return false;
        else {
            Namespace them = (Namespace)obj;
            return this.getName().equals(them.getName());
        }
    }
    
    /**
     * {@inheritDocs}
     */
    public int hashCode() {
        int hash = 17;
        return 31*hash + this.getName().hashCode();
    }
    
    /**
     * {@inheritDocs}
     */
    public String toString() { return this.getName(); }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
}