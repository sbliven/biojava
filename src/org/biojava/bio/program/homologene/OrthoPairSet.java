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

package org.biojava.bio.program.homologene;

import java.util.Set;

/**
 * represents the Homologene Group.
 */
public interface OrthoPairSet
{
    public interface Iterator
    {
        public boolean hasNext();

        public OrthoPair nextOrthoPair();
    }

    /**
     * retrieves name of this group.
     * Homologene itself does not assign names
     * or identifiers to groups.
     */
    public String getName();

    /**
     * set the name of this group.
     */
    public void setName(String name);

    /**
     * adds a specified OrthoPair relationship
     * to this group.
     */
    public void addOrthoPair(OrthoPair orthology);

    /**
     * removes a specified OrthoPair relationship
     * from this group.
     */
    public void removeOrthoPair(OrthoPair orthology);

    /**
     returns an iterator to the contents of the set.
    /**
     * no. of entries in this Homologene group
     */
    public int size();

    /**
     * returns an iterator to the members of this set
     */
    public Iterator iterator();
    /**
     * get the taxa represented in this group
     */
    public Set getTaxa();

    /**
     * get the lowest level of identity observed
     * in this Group
     */
    public double getMinIdentity();
}

