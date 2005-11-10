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

package org.biojavax;
import java.util.Iterator;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.bio.BioEntry;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.InifinitelyAmbiguousSymbolList;
import org.biojavax.bio.seq.RichSequence;

/**
 * A simple implementation of CrossReferenceResolver
 * @author Richard Holland
 * @author Mark Schreiber
 * @since 1.5
 */
public class SimpleCrossReferenceResolver  implements CrossReferenceResolver {
    
    /**
     * {@inheritDoc}
     */
    public SymbolList getRemoteSymbolList(CrossRef cr, Alphabet a) {
        BioEntry be = this.getRemoteBioEntry(cr);
        if (be instanceof RichSequence) return (RichSequence)be;
        // If we get here we didn't find it, so we must create a dummy sequence instead
        if (!(a instanceof FiniteAlphabet)) throw new IllegalArgumentException("Cannot construct dummy symbol list for a non-finite alphabet");
        return new InifinitelyAmbiguousSymbolList((FiniteAlphabet)a);
    }
    
    /**
     * {@inheritDoc}
     */
    public BioEntry getRemoteBioEntry(CrossRef cr){
        Namespace ns = (Namespace)RichObjectFactory.getObject(SimpleNamespace.class, new Object[]{cr.getDbname()});
        String accession = cr.getAccession();
        for (Iterator i = ns.getMembers().iterator(); i.hasNext(); ) {
            BioEntry be = (BioEntry)i.next();
            if (be.getAccession().equals(accession) && be.getVersion() == cr.getVersion()) return be;
        }
        return null;
    }
}

