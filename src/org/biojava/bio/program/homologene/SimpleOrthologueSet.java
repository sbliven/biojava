/*
 *                    BioJava development code
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

package org.biojava.bio.program.homologene;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class SimpleOrthologueSet implements OrthologueSet
{

    public class Iterator implements OrthologueSet.Iterator
    {
        private java.util.Iterator orthoIterator;

        private Iterator(java.util.Iterator orthoIterator)
        {
            this.orthoIterator = orthoIterator;
        }

        public boolean hasNext()
        {
            return orthoIterator.hasNext();
        }

        public Orthologue nextOrthologue()
        {
            return (Orthologue) orthoIterator.next();
        }

    }

    // every Orthologue is stored in a Set
    private Set orthologueSet = new HashSet();
    private Map orthologueByHomologeneID = new HashMap();

    public Orthologue createOrthologue(int taxonID, String locusID, String homologeneID, String accession)
        throws IllegalArgumentException
    {
        // create the Orthologue
        Orthologue newOrthologue = new SimpleOrthologue(taxonID, locusID, homologeneID, accession);

        // stash it
        orthologueSet.add(newOrthologue);
        orthologueByHomologeneID.put(homologeneID, newOrthologue);
        return newOrthologue;
    }

    public Orthologue getOrthologue(String homologeneID)
    {
        return (Orthologue) orthologueByHomologeneID.get(homologeneID);
    }

    public Iterator iterator()
    {
        return new Iterator(orthologueSet.iterator());
    }
}

