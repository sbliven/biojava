/**
 * BioJava development code
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

package org.biojava.bio.seq.db.gadfly;

import java.util.Iterator;
import java.util.List;

import org.biojava.bio.BioError;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

import org.biojava.utils.ChangeVetoException;

/**
 * This implements very basic sequences from
 * Gadfly.  Basically, these may/may not have
 * symbols and are NOT assemblies.
 *
 * @author David Huen
 */
public class GFBasicSequence
    extends GFFeatureSetFeatureHolder
    implements Sequence
{
    private int seq_id;

    public GFBasicSequence(GadflyDB parentDB, int seq_id)
    {
        // this gets all immediate children of this sequence
        super(parentDB,
            "SELECT id FROM seq_feature LEFT OUTER JOIN sf_produces_sf "
            + "ON seq_feature.id=sf_produces_sf.produces_sf_id "
            + "WHERE src_seq_id=" + seq_id + " AND sf_produces_sf.produces_sf_id IS NULL");
    }

    private Sequence getSequence()
    {
        // it's not exactly easy to walk away
        // from failure here....
        try {
        return parentDB.getSequence(seq_id);
        }
        catch (Exception e) {
            throw new BioError(e);
        }
    }

    /*****************************
     * Sequence-specific methods *
     *****************************/

    public String getName()
    {
        return getSequence().getName();
    }

    public String getURN()
    {
        return getSequence().getURN(); 
    }

    /**********************
     * SymbolList methods *
     **********************/
    public void edit(Edit edit)
        throws ChangeVetoException
    {
        throw new ChangeVetoException();
    }

    public Alphabet getAlphabet()
    {
        return getSequence().getAlphabet();
    }

    public Iterator iterator()
    {
        return getSequence().iterator();
    }

    public int length()
    {
        return getSequence().length();
    }

    public String seqString()
    {
        return getSequence().seqString();
    }

    public SymbolList subList(int start, int end)
    {
        return getSequence().subList(start, end);
    }

    public String subStr(int start, int end)
    {
        return getSequence().subStr(start, end);
    }

    public Symbol symbolAt(int pos)
    {
        return getSequence().symbolAt(pos);
    }

    public List toList()
    {
        return getSequence().toList();
    }

    /***********************
     * Annotatable methods *
     ***********************/

    public Annotation getAnnotation()
    {
        return Annotation.EMPTY_ANNOTATION;
    }
}

