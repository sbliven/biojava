// BiblioEntryStatus.java
//
//    senger@ebi.ac.uk
//    March 2001
//

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
package org.biojava.bibliography;

import java.util.*;

/**
 * It defines information related to the citation itself rather than to the cited resource.
 * In other words, it represents a status of a record in a bibliographic repository.
 *<P>
 * @author <A HREF="mailto:senger@ebi.ac.uk">Martin Senger</A>
 * @version $Id$
 */

public class BiblioEntryStatus {

    /**
     * The dynamic properties may be used to add features related to
     * the citation itself.  For example, a name of the citation
     * annotator, or citation version.
     *<P>
     * The property names should be made available in a controlled
     * vocabulary named {@link BibRefSupport#ENTRY_PROPERTIES}.
     */
    public Hashtable properties = new Hashtable();

    /**
     * It defines when the citation record was added or last modified.
     * Usually it is used to retrieve new or revised (since a specified date) citations.
     *<P>
     * @see BibRef#date date in BibRef for format discussion
     */ 
    public String lastModifiedDate;

    /**
     * Some bibliographic repositories consist of several, or even
     * many, databases.  The subset helps to locate the citation
     * within the repository.
     *
     *<P>
     * The possible values of this member should be defined in a
     * controlled vocabulary named {@link BibRefSupport#REPOSITORY_SUBSETS}.
     */
    public String repositorySubset;

}
