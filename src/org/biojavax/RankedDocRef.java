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
 * RankedDocRef.java
 *
 * Created on July 12, 2005, 8:10 AM
 */

package org.biojavax;

/**
 * Represents a documentary reference, the bioentryreference table in BioSQL.
 * @author Richard Holland
 */
public interface RankedDocRef extends Comparable {
    
    /**
     * Represents a reference to a document.
     * @return the document reference.
     */
    public DocRef getDocumentReference();
    
    /**
     * The start position in the sequence that this reference refers to.
     * @return the start position.
     */
    public int getStart();
    
    /**
     * The end position in the sequence that this reference refers to.
     * @return the end position.
     */
    public int getEnd();
    
    /**
     * The rank of this reference.
     * @return the rank.
     */
    public int getRank();
}


