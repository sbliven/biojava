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

 * BioEntryRelationship.java

 *

 * Created on June 14, 2005, 5:33 PM

 */



package org.biojavax.bio;

import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;

import org.biojavax.ontology.ComparableTerm;



/**

 * Represents the relation between two bioentries. The bioentry_relationship in 

 * BioSQL is what this represents.

 * @author Mark Schreiber

 * @author Richard Holland

 */

public interface BioEntryRelationship extends Comparable,Changeable {

    public static final ChangeType RANK = new ChangeType(

            "This bioentry relationship's rank has changed",

            "org.biojavax.bio.BioEntryRelationship",

            "rank"

            );
    
    public void setRank(int rank) throws ChangeVetoException;
    
    public int getRank();
    
    /**

     * Getter for property object.

     * @return Value of property object.

     */

    public BioEntry getObject();



    /**

     * Getter for property subject.

     * @return Value of property subject.

     */

    public BioEntry getSubject();



    /**

     * Getter for property term.

     * @return Value of property term.

     */

    public ComparableTerm getTerm();



}

