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


package org.biojava.bio.seq.db.biosql;

import org.biojava.utils.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;


public class BioSQLFeatureChangeHub extends IndexedChangeHub
{
    BioSQLSequenceDB seqDB;
    BioSQLEntryChangeHub entryHub;

    public BioSQLFeatureChangeHub(BioSQLSequenceDB seqDB, BioSQLEntryChangeHub entryHub)
    {
        super();
        this.seqDB = seqDB;
        this.entryHub = entryHub;
    }

    protected final boolean isMyChangeEvent(ChangeEvent cev, ListenerMemento lm)
    {
        ChangeType ct = cev.getType();
        return ct.isMatchingType(lm.type);
    }

    void firePreChange(ChangeEvent cev)
        throws ChangeVetoException
    {
        BioSQLFeature source = (BioSQLFeature) cev.getSource();
        Integer bioentry_id = new Integer(source._getInternalID());

        super.firePreChange(bioentry_id, cev);

        FeatureHolder parent = source.getParent();
        ChangeEvent pcev = new ChangeEvent(parent, FeatureHolder.FEATURES, null, null, cev);
        if (parent instanceof Feature) {
            firePreChange(pcev);
        } else {
            entryHub.firePreChange(pcev);
        }
    }

    void firePostChange(ChangeEvent cev)
    {
        BioSQLFeature source = (BioSQLFeature) cev.getSource();
        Integer bioentry_id = new Integer(source._getInternalID());

        super.firePostChange(bioentry_id, cev);

        FeatureHolder parent = source.getParent();
        ChangeEvent pcev = new ChangeEvent(parent, FeatureHolder.FEATURES, null, null, cev);
        if (parent instanceof Feature) {
            firePostChange(pcev);
        } else {
            entryHub.firePostChange(pcev);
        }
    }
}

