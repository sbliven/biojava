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
import java.util.Set;
import java.util.HashSet;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.FeatureFilter;

import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeVetoException;

/**
 * this class exists to hold result of filter()
 * operations in Gadfly.
 */
class GFFeatureHolder
    extends AbstractFeatureHolder
    implements FeatureHolder
{
    // holds the sf_ids of all features
    // held by this holder
    protected Set featureSet;

    protected GadflyDB parentDB;

    protected class FeatureSetIterator implements Iterator
    {
        Iterator setIterator;

        protected FeatureSetIterator()
        {
            setIterator = featureSet.iterator();
        }

        public boolean hasNext()
        {
            return setIterator.hasNext();
        }

        public Object next()
        {
            try {
                // return the object of given id
                return parentDB.createFeature(((Integer) setIterator.next()).intValue());
            }
            catch (NullPointerException npe) {
                return null;
            }
        }

        public void remove()
           throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }
    }

    GFFeatureHolder(GadflyDB parentDB)
    {
        super();

        this.parentDB = parentDB;
        featureSet = new HashSet();
    }

    /**
     * in this version, a feature is only said
     * to be contained by this FeatureHolder if
     * it actually comes from this GadflyDB.
     */
    public boolean containsFeature(Feature f)
    {
        if (f instanceof GFFeature) {
            return featureSet.contains(new Integer( ((GFFeature) f).getID()) );
        }
        else
            return false;
    }

    public int countFeatures()
    {
        return featureSet.size();
    }
/*
    public FeatureFilter getSchema()
    {
        // change this to be more informative later
        return FeatureFilter.all;
    }
*/
    public Iterator features()
    {
        return new FeatureSetIterator(); 
    }

    /*************************************
     * Additional class-specific methods *
     *************************************/

    /**
     * add a feature to an existing
     * GFFeatureFilter.
     */
    void addFeature(GFFeature feature)
        throws ChangeVetoException
    {
        featureSet.add(new Integer(feature.getID()));
    }

    void addFeatureID(int sf_id)
    {
        featureSet.add(new Integer(sf_id));
    }

    /**
     * return the internal id set that
     * maintains state for this FeatureHolder
     */
    Set getFeatureSet()
    {
        return featureSet;
    }

    protected GFFeatureHolder createFeatureHolder()
    {
        return new GFFeatureHolder(parentDB);
    }
}

