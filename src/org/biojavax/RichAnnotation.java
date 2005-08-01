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
 * RichAnnotation.java
 *
 * Created on July 29, 2005, 10:18 AM
 */

package org.biojavax;

import java.util.NoSuchElementException;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.utils.ChangeVetoException;


/**
 * An annotation which can have ranked terms. All keys must be
 * ComparableTerm objects, and all values must be Strings.
 * @author Richard Holland
 */
public interface RichAnnotation extends Annotation {
    
    /**
     * Removes all notes from this annotation object.
     */
    public void clear();
    
    /**
     * Adds a note to this annotation.
     * @param note note to add
     * @throws ChangeVetoException if it doesn't like this.
     */
    public void addNote(Note note) throws ChangeVetoException;
    
    /**
     * Removes a note from this annotation.
     * @param note note to remove
     * @throws ChangeVetoException if it doesn't like this.
     */
    public void removeNote(Note note) throws ChangeVetoException;
    
    /**
     * Uses the term and rank to lookup a note in this annotation.
     * @param note note to lookup, using term and rank.
     * @return the matching note.
     * @throws ChangeVetoException if it doesn't like this.
     * @throws NoSuchElementException if it doesn't exist.
     */
    public Note getNote(Note note) throws NoSuchElementException;
    
    /**
     * Returns true if the given note exists in this annotation.
     * The lookup is done using the term and rank of the note.
     * @param note note to lookup
     * @return true if it is in this annotation, false if not.
     */
    public boolean contains(Note note);
    
    /**
     * Returns an immutable set of all notes in this annotation.
     * @return a set of notes.
     */
    public Set getNoteSet();
    
    /**
     * Clears the notes from this annotation and replaces them with
     * those from the given set.
     * @param notes notes to use from now on.
     * @throws ChangeVetoException if it doesn't like any of them.
     */
    public void setNoteSet(Set notes) throws ChangeVetoException;
}
