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
 * CrossRef.java
 *
 * Created on June 14, 2005, 4:53 PM
 */

package org.biojavax;

import org.biojava.utils.Changeable;

/**
 * Represents a cross reference to another database, the dbxref table in BioSQL.
 * @author Mark Schreiber
 * @author Richard Holland
 */
public interface CrossRef extends RichAnnotatable,Comparable,Changeable {
    
   /**
     * Getter for property dbname.
     * @return Value of property dbname.
     */
    public String getDbname();

    /**
     * Getter for property accession.
     * @return Value of property accession.
     */
    public String getAccession();

    /**
     * Getter for property version.
     * @return Value of property version.
     */
    public int getVersion();
    
}

