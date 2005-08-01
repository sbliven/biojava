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
 * SimpleRichAnnotation.java
 *
 * Created on July 29, 2005, 10:30 AM
 */

package org.biojavax;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import org.biojava.bio.Annotatable;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.ontology.ComparableTerm;

/**
 * Simple annotation wrapper. All non-note annotations get a rank of zero.
 *
 * @author Richard Holland
 */
public class SimpleRichAnnotation extends AbstractChangeable implements RichAnnotation {
    
    private Set notes = new TreeSet(); // Keeps them ordered by rank then term
    
    /** Creates a new instance of SimpleRichAnnotation */
    public SimpleRichAnnotation() {
    }
    
    /**
     * {@inheritDocs}
     */
    public void clear() { this.notes.clear(); }
    
    /**
     * {@inheritDocs}
     */
    public Map asMap() {
        Map m = new HashMap();
        for (Iterator i = this.notes.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            m.put(n.getTerm(), n.getValue());
        }
        return m;
    }
    
    /**
     * {@inheritDocs}
     */
    public void addNote(Note note) throws ChangeVetoException {
        if(!this.hasListeners(Annotatable.ANNOTATION)) {
            this.notes.add(note);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    Annotatable.ANNOTATION,
                    note,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(Annotatable.ANNOTATION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.notes.add(note);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    private Note dummyNote(Object key) { return new SimpleNote((ComparableTerm)key,null,0); }
    
    /**
     * {@inheritDocs}
     */
    public boolean contains(Note note) { return this.notes.contains(note); }
    
    /**
     * {@inheritDocs}
     */
    public boolean containsProperty(Object key) { return this.contains(this.dummyNote(key)); }
    
    /**
     * {@inheritDocs}
     */
    public Note getNote(Note note) throws NoSuchElementException {
        for (Iterator i = this.notes.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            if (note.equals(n)) return n;
        }
        throw new NoSuchElementException("No such property: "+note.getTerm().getName()+", rank "+note.getRank());
    }
    
    /**
     * {@inheritDocs}
     */
    public Object getProperty(Object key) throws NoSuchElementException { return this.getNote(this.dummyNote(key)).getValue(); }
    
    /**
     * {@inheritDocs}
     */
    public Set keys() { return this.asMap().keySet(); }
    
    /**
     * {@inheritDocs}
     */
    public void removeNote(Note note) throws ChangeVetoException {
        if(!this.hasListeners(Annotatable.ANNOTATION)) {
            this.notes.remove(note);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    Annotatable.ANNOTATION,
                    null,
                    note
                    );
            ChangeSupport cs = this.getChangeSupport(Annotatable.ANNOTATION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.notes.remove(note);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDocs}
     */
    public void removeProperty(Object key) throws NoSuchElementException, ChangeVetoException { this.removeNote(this.dummyNote(key)); }
    
    /**
     * {@inheritDocs}
     */
    public void setProperty(Object key, Object value) throws IllegalArgumentException, ChangeVetoException {
        Note n = this.dummyNote(key);
        n.setValue((String)value);
        this.addNote(n);
    }
    
    /**
     * {@inheritDocs}
     */
    public Set getNoteSet() { return Collections.unmodifiableSet(this.notes); }
    
    /**
     * {@inheritDocs}
     */
    public void setNoteSet(Set notes) throws ChangeVetoException {
        this.notes.clear();
        for (Iterator i = notes.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (!(o instanceof Note)) throw new ChangeVetoException("Cannot add non-Note objects as notes");
            this.addNote((Note)o);
        }
    }
}
