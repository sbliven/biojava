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

package org.biojavax.bio.seq;
import java.util.Iterator;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.CrossRef;
import org.biojavax.Namespace;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.db.RichObjectFactory;

/**
 * This returns symbols for local sequences, or an equivalent set
 * of 'n's for remote ones. It only works for contiguous locations.
 * @author Richard Holland
 */
public class SimpleRichLocationResolver implements RichLocationResolver {
    
    /**
     * {@inheritDoc}
     */
    public SymbolList getRemoteSymbolList(CrossRef cr, Alphabet a) {
        Namespace ns = (Namespace)RichObjectFactory.getObject(Namespace.class, new Object[]{cr.getDbname()});
        String accession = cr.getAccession();
        for (Iterator i = ns.getMembers().iterator(); i.hasNext(); ) {
            BioEntry be = (BioEntry)i.next();
            if (be instanceof RichSequence) {
                RichSequence rs = (RichSequence)be;
                if (rs.getAccession().equals(accession) && rs.getAlphabet().equals(a)) return rs;
            }
        }
        // If we get here we didn't find it, so we must create a dummy sequence instead
        if (!(a instanceof FiniteAlphabet)) throw new IllegalArgumentException("Cannot construct dummy symbol list for a non-finite alphabet");
        return new InifinitelyAmbiguousSymbolList((FiniteAlphabet)a);
    }
    
}
