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

import org.biojava.utils.ChangeVetoException;

/**
 * represents the Homologene Group.
 */
public abstract class AbstractOrthoPairSet
    implements OrthoPairSet
{

    public abstract String getName();

    public abstract void setName(String name);

    public abstract void addOrthoPair(OrthoPair orthology) throws ChangeVetoException;

    public abstract void removeOrthoPair(OrthoPair orthology);

    public abstract int size();

    public abstract Iterator iterator();

    public abstract Set getTaxa();

    public abstract double getMinIdentity();

    public OrthoPairSet filter(OrthoPairFilter filter)
    {
        OrthoPairSet results = new SimpleOrthoPairSet();

        for (Iterator pairsI = iterator();
               pairsI.hasNext(); )
        {
            OrthoPair pair = pairsI.nextOrthoPair();

            if (filter.accept(pair)) {
                try {
                    results.addOrthoPair(pair);
                }
                catch (ChangeVetoException cve) {
                    // should be impossible as this group was created by me
                }
            }
        }
        return results;
    }
}

