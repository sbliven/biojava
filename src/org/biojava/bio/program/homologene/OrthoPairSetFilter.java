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

public interface OrthoPairSetFilter
{
    public boolean accept(OrthoPairSet group);

    public final static class AcceptAll implements OrthoPairSetFilter
    {
        public boolean accept(OrthoPairSet group) { return true; }
    }

    public final static class ByTaxon implements OrthoPairSetFilter
    {
        Taxon taxon;

        public ByTaxon(Taxon taxon)
        {
            this.taxon = taxon;
        }

        public boolean accept(OrthoPairSet group)
        {
            Set taxa = group.getTaxa();

            return taxa.contains(taxon);
        }
    }

    public final static class ByMinIdentity implements OrthoPairSetFilter
    {
        double minValue;

        public ByMinIdentity(double minValue)
        {
            this.minValue = minValue;
        }

        public boolean accept(OrthoPairSet group)
        {
            return (group.getMinIdentity() >= minValue);
        }
    }

}
