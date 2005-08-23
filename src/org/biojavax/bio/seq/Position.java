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
 * Position.java
 *
 * Created on July 28, 2005, 5:29 PM
 */
package org.biojavax.bio.seq;


/**
 * Holds enough info about positions to keep BioSQL happy if needs be.
 *
 * @author Richard Holland
 */
public interface Position {
    
    public static final String BETWEEN_BASES = "^";
    public static final String IN_RANGE = ".";
    
    public boolean hasFuzzyStart();
    public boolean hasFuzzyEnd();
    public int getStart();
    public int getEnd();
    public Position translate(int distance);
    public String getType();
}
