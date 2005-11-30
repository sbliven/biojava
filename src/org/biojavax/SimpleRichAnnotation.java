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
import java.util.TreeMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import org.biojava.bio.Annotatable;
import org.biojava.ontology.Term;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.ontology.ComparableTerm;

/**
 * Simple annotation wrapper. All non-Note annotations get a rank of zero.
 * @author Richard Holland
 */
public class SimpleRichAnnotation extends AbstractChangeable implements RichAnnotation {
    
    private Set notes = new TreeSet(); // Keeps them ordered by rank then term
    
    /** Creates a new, empty instance of SimpleRichAnnotation */
    public SimpleRichAnnotation() {}
    
    /**
     * {@inheritDoc}
     */
    public void clear() throws ChangeVetoException{ 
        for(Iterator i = this.notes.iterator(); i.hasNext(); ){
            this.removeNote((Note)i.next());
        }
    }
    
    /**
     * {@inheritDoc}
     * The map is a copy of the internal structure. It is a map of 
     * <code>ComparableTerm</code>s to <code>String</code>s corresponding
     * to the Term and Value of the <code>Note</code>s in the annotation.
     */
    public Map asMap() {
        Map m = new TreeMap();
        for (Iterator i = this.notes.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            m.put(n.getTerm(), n.getValue());
        }
        return m;
    }
    
    /**
     * {@inheritDoc}
     */
    public void addNote(Note note) throws ChangeVetoException {
        if (note==null) throw new IllegalArgumentException("Note cannot be null");
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
    
    // A dummy note is a Note object with the given key and no value. It is used
    // for purposes of comparing/converting non-Note annotations. The string
    // value of the key is created in the default ontology as a Term, unless the key
    // is already a term, in which case the Term is imported to the default ontology.
    private Note dummyNote(Object key) {
        if (key==null) throw new IllegalArgumentException("Key cannot be null"); 
        if (!(key instanceof ComparableTerm)) {
            if (key instanceof Term) key = RichObjectFactory.getDefaultOntology().getOrImportTerm((Term)key);
            else key = RichObjectFactory.getDefaultOntology().getOrCreateTerm(key.toString());
        }
        return new SimpleNote((ComparableTerm)key,null,0); 
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(Note note) { return this.notes.contains(note); }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsProperty(Object key) { return this.contains(this.dummyNote(key)); }
    
    /**
     * {@inheritDoc}
     */
    public Note getNote(Note note) throws NoSuchElementException {
        if (note==null) throw new IllegalArgumentException("Note cannot be null");
        for (Iterator i = this.notes.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            if (note.equals(n)) return n;
        }
        throw new NoSuchElementException("No such property: "+note.getTerm()+", rank "+note.getRank());
    }
    
    /**
     * {@inheritDoc}
     * Strictly it will return the <code>Note</code> which matches the 
     * <code>key</code> (or a <code>Term</code> made with a <code>String</code> key)
     * with a rank of 0.
     * @see #getProperties(Object key)
     */
    public Object getProperty(Object key) throws NoSuchElementException { return this.getNote(this.dummyNote(key)).getValue(); }
    
    /**
     * {@inheritDoc}
     */
    public Note[] getProperties(Object key){
        ComparableTerm term = dummyNote(key).getTerm();
        List l = new LinkedList();
        for(Iterator i = notes.iterator(); i.hasNext();){
            Note n = (Note)i.next();
            if(n.getTerm().equals(term)){
                l.add(n);
            }
        }
        Collections.sort(l);
        Note[] na = new Note[l.size()];
        l.toArray(na);
        return na;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set keys() { return this.asMap().keySet(); }
    
    /**
     * {@inheritDoc}
     */
    public void removeNote(Note note) throws ChangeVetoException {
        if (note==null) throw new IllegalArgumentException("Note cannot be null");
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
     * {@inheritDoc}
     */
    public void removeProperty(Object key) throws NoSuchElementException, ChangeVetoException { this.removeNote(this.dummyNote(key)); }
    
    /**
     * {@inheritDoc}
     */
    public void setProperty(Object key, Object value) throws IllegalArgumentException, ChangeVetoException {
        Note n = this.dummyNote(key);
        n.setValue(value.toString());
        this.addNote(n);
    }
    
    /**
     * {@inheritDoc}
     * <b>Warning</b> this method gives access to the original 
     * Collection not a copy. This is required by Hibernate. If you
     * modify the object directly the behaviour may be unpredictable.
     */
    public Set getNoteSet() {  return this.notes; } // original for Hibernate
    
    /**
     * {@inheritDoc}
     * <b>Warning</b> this method gives access to the original 
     * Collection not a copy. This is required by Hibernate. If you
     * modify the object directly the behaviour may be unpredictable.
     */
    public void setNoteSet(Set notes) throws ChangeVetoException { this.notes = notes; } // original for Hibernate
    
    /**
     * {@inheritDoc}
     * Form: list of "[note]" values separated by commas
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator i = this.notes.iterator(); i.hasNext(); ) {
            sb.append("[");
            sb.append(i.next());
            sb.append("]");
            if (i.hasNext()) sb.append(",");
        }
        return sb.toString();
    }
}
