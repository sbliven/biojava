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

package org.biojava.bio.seq;

import java.util.Iterator;
import java.util.List;

import org.biojava.bio.Annotatable;
import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;

/**
 * @author Thomas Down
 * @author David Allen
 */
public class DummySequence extends AbstractChangeable
    implements Sequence 
{
    private String urn;
    private String name;

    private FeatureHolder features;
    private SymbolList    symbols;
    private Annotation    annotation;

    public DummySequence(String urn, String name)
    {
        this.urn  = urn;
        this.name = name;

        features   = FeatureHolder.EMPTY_FEATURE_HOLDER;
        symbols    = SymbolList.EMPTY_LIST;
        annotation = Annotation.EMPTY_ANNOTATION;

        // Lock all delegates to prevent changes
        features.addChangeListener(ChangeListener.ALWAYS_VETO);
        symbols.addChangeListener(ChangeListener.ALWAYS_VETO);
        annotation.addChangeListener(ChangeListener.ALWAYS_VETO);
    }

    public Annotation getAnnotation()
    {
        return annotation;
    }

    public int length()
    {
        return symbols.length();
    }

    public Iterator iterator()
    {
        return symbols.iterator();
    }

    public SymbolList subList(int start, int end)
        throws IndexOutOfBoundsException
    {
        return symbols.subList(start, end);
    }

    public Alphabet getAlphabet()
    {
        return symbols.getAlphabet();
    }

    public Symbol symbolAt(int index) throws IndexOutOfBoundsException
    {
        return symbols.symbolAt(index);
    }

    public List toList()
    {
        return symbols.toList();
    }

    public String seqString()
    {
        return symbols.seqString();
    }

    public String subStr(int start, int end)
        throws IndexOutOfBoundsException
    {
        return symbols.subStr(start, end);
    }

    public void edit(Edit edit)
        throws IndexOutOfBoundsException, IllegalAlphabetException,
               ChangeVetoException
    {
        symbols.edit(edit);
    }

    public String getName()
    {
        return name;
    }

    public String getURN()
    {
        return urn;
    }

    public int countFeatures()
    {
        return features.countFeatures();
    }

    public Iterator features()
    {
        return features.features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse)
    {
        return features.filter(ff, recurse);
    }

    public FeatureHolder filter(FeatureFilter ff) {
        return features.filter(ff);
    }
    
    public Feature createFeature(Feature.Template template)
        throws BioException, ChangeVetoException
    {
        return features.createFeature(template);
    }

    public void removeFeature(Feature feature) throws ChangeVetoException
    {
        features.removeFeature(feature);
    }

    public boolean containsFeature(Feature feature)
    {
        return features.containsFeature(feature);
    }
    
    public FeatureFilter getSchema() {
        return features.getSchema();
    }
}
