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


import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

public class HomoloGroupSet
{
    Set groups;

    public class GroupIterator
    {
        private Iterator groupsI;

        /**
         * constructor where the iterator is already created
         */
        private GroupIterator(Iterator groupsI) { this.groupsI = groupsI; }

        public boolean hasNext()
        {
            return groupsI.hasNext();
        }

        public HomoloGroup nextGroup()
        {
            return (HomoloGroup) groupsI.next();
        }
    }

    public HomoloGroupSet()
    {
        groups = new HashSet();
    }

    HomoloGroupSet(Set groups)
    {
        this.groups = groups;
    }

    public void add(HomoloGroup group)
    {
        groups.add(group);
    }

    public boolean contains(HomoloGroup group)
    {
        return groups.contains(group);
    }

    public boolean isEmpty() { return groups.isEmpty(); }

    public GroupIterator iterator()
    {
        return new GroupIterator(groups.iterator());
    }
}

