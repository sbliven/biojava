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

import java.lang.ref.*;
import java.util.*;
import java.sql.*;

import org.biojava.utils.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.*;

/**
 * Hub for changevents originating in BioSQL objects.  Bit nasty
 * and monolithic.  There should be a generic `hub' class with one
 * instance for each event type, and handle the event forwarding
 * and locking separately.  This implementation works well for now,
 * though.
 *
 * @author Thomas Down
 * @since 1.3
 */

class BioSQLChangeHub {
    private final Map entryAnnotationListeners;
    private final Map featureAnnotationListeners;
    private final Map featureListeners;
    private final Map entryListeners;
    private final List databaseListeners;
    private final ReferenceQueue queue;
    private final BioSQLSequenceDB seqDB;
    
    BioSQLChangeHub(BioSQLSequenceDB seqDB) {
	super();
	entryAnnotationListeners = new HashMap();
	featureAnnotationListeners = new HashMap();
	featureListeners = new HashMap();
	entryListeners = new HashMap();
	databaseListeners = new ArrayList();
	queue = new ReferenceQueue();
	this.seqDB = seqDB;
    }

    private void diddleQueue() {
	Reference ref;
	while ((ref = queue.poll()) != null) {
	    if (ref instanceof FeatureListenerReference) {
		List listenerList = (List) featureListeners.get(((FeatureListenerReference) ref).getKey());
		if (listenerList != null) {
		    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
			ListenerMemento lm = (ListenerMemento) i.next();
			if (lm.listener == ref) {
			    i.remove();
			    break;
			}
		    }
		}
	    } else if (ref instanceof EntryListenerReference) {
		List listenerList = (List) entryListeners.get(((EntryListenerReference) ref).getKey());
		if (listenerList != null) {
		    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
			ListenerMemento lm = (ListenerMemento) i.next();
			if (lm.listener == ref) {
			    i.remove();
			    break;
			}
		    }
		}
	    } else if (ref instanceof FeatureAnnotationListenerReference) {
		List listenerList = (List) featureAnnotationListeners.get(((FeatureAnnotationListenerReference) ref).getKey());
		if (listenerList != null) {
		    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
			ListenerMemento lm = (ListenerMemento) i.next();
			if (lm.listener == ref) {
			    i.remove();
			    break;
			}
		    }
		}
	    } else if (ref instanceof EntryAnnotationListenerReference) {
		List listenerList = (List) entryAnnotationListeners.get(((EntryAnnotationListenerReference) ref).getKey());
		if (listenerList != null) {
		    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
			ListenerMemento lm = (ListenerMemento) i.next();
			if (lm.listener == ref) {
			    i.remove();
			    break;
			}
		    }
		}
	    } else {
		for (Iterator i = databaseListeners.iterator(); i.hasNext(); ) {
		    ListenerMemento lm = (ListenerMemento) i.next();
		    if (lm.listener == ref) {
			i.remove();
			break;
		    }
		}
	    }
	}
    }

    public synchronized void addEntryAnnotationListener(int bioentry_id,
							ChangeListener listener,
							ChangeType ct)
    {
	diddleQueue();
	Integer id = new Integer(bioentry_id);
	List listenerList = (List) entryAnnotationListeners.get(id);
	if (listenerList == null) {
	    listenerList = new ArrayList();
	    featureListeners.put(id, listenerList);
	}
	listenerList.add(new ListenerMemento(ct, new EntryAnnotationListenerReference(id, listener, queue)));
    }

    public synchronized void removeEntryAnnotationListener(int bioentry_id,
							   ChangeListener listener,
							   ChangeType ct)
    {
	Integer id = new Integer(bioentry_id);
	List listenerList = (List) entryAnnotationListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct == lm.type && listener.equals(lm.listener.get())) {
		    lm.listener.clear();
		    i.remove();
		    return;
		}
	    }
	}

	// Is this an error?
    }

    
    public void fireEntryAnnotationPreChange(ChangeEvent cev) 
        throws ChangeVetoException
    {
	BioSQLSequenceAnnotation source = (BioSQLSequenceAnnotation) cev.getSource();
	Integer id = new Integer(source.getBioentryID());
	ChangeType ct = cev.getType();
	List listenerList = (List) entryAnnotationListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct.isMatchingType(lm.type)) {
		    ChangeListener cl = (ChangeListener) lm.listener.get();
		    if (cl != null) {
			cl.preChange(cev);
		    }
		}
	    }
	}	

	try {
	    Sequence seq = seqDB.getSequence(null, id.intValue());
	    ChangeEvent pcev = new ChangeEvent(seq, Annotatable.ANNOTATION, null, null, cev);
	    fireEntryPreChange(pcev);
	} catch (BioException ex) {
	    throw new BioRuntimeException("Sequence has gone missing");
	}
    }

    public void fireEntryAnnotationPostChange(ChangeEvent cev) 
    {
	BioSQLSequenceAnnotation source = (BioSQLSequenceAnnotation) cev.getSource();
	Integer id = new Integer(source.getBioentryID());
	ChangeType ct = cev.getType();
	List listenerList = (List) entryAnnotationListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct.isMatchingType(lm.type)) {
		    ChangeListener cl = (ChangeListener) lm.listener.get();
		    if (cl != null) {
			cl.postChange(cev);
		    }
		}
	    }
	}	

	try {
	    Sequence seq = seqDB.getSequence(null, id.intValue());
	    ChangeEvent pcev = new ChangeEvent(seq, Annotatable.ANNOTATION, null, null, cev);
	    fireEntryPostChange(pcev);
	} catch (BioException ex) {
	    throw new BioRuntimeException("Sequence has gone missing");
	}
    }

    public synchronized void addFeatureAnnotationListener(int feature_id,
							  ChangeListener listener,
							  ChangeType ct)
    {
	diddleQueue();
	Integer id = new Integer(feature_id);
	List listenerList = (List) featureAnnotationListeners.get(id);
	if (listenerList == null) {
	    listenerList = new ArrayList();
	    featureListeners.put(id, listenerList);
	}
	listenerList.add(new ListenerMemento(ct, new FeatureAnnotationListenerReference(id, listener, queue)));
    }

    public synchronized void removeFeatureAnnotationListener(int feature_id,
							     ChangeListener listener,
							     ChangeType ct)
    {
	Integer id = new Integer(feature_id);
	List listenerList = (List) featureAnnotationListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct == lm.type && listener.equals(lm.listener.get())) {
		    lm.listener.clear();
		    i.remove();
		    return;
		}
	    }
	}

	// Is this an error?
    }

    public void fireFeatureAnnotationPreChange(ChangeEvent cev) 
        throws ChangeVetoException
    {
	BioSQLFeatureAnnotation source = (BioSQLFeatureAnnotation) cev.getSource();
	Integer id = new Integer(source.getFeatureID());
	ChangeType ct = cev.getType();
	List listenerList = (List) featureAnnotationListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct.isMatchingType(lm.type)) {
		    ChangeListener cl = (ChangeListener) lm.listener.get();
		    if (cl != null) {
			cl.preChange(cev);
		    }
		}
	    }
	}	

	Feature parent = seqDB.getFeatureByID(id.intValue());
	ChangeEvent pcev = new ChangeEvent(parent, Annotatable.ANNOTATION, null, null, cev);
	fireFeaturePreChange(pcev);
    }

    public void fireFeatureAnnotationPostChange(ChangeEvent cev) 
    {
	BioSQLFeatureAnnotation source = (BioSQLFeatureAnnotation) cev.getSource();
	Integer id = new Integer(source.getFeatureID());
	ChangeType ct = cev.getType();
	List listenerList = (List) featureAnnotationListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct.isMatchingType(lm.type)) {
		    ChangeListener cl = (ChangeListener) lm.listener.get();
		    if (cl != null) {
			cl.postChange(cev);
		    }
		}
	    }
	}	

	Feature parent = seqDB.getFeatureByID(id.intValue());
	ChangeEvent pcev = new ChangeEvent(parent, Annotatable.ANNOTATION, null, null, cev);
	fireFeaturePostChange(pcev);
    }


    public synchronized void addFeatureListener(int feature_id,
						ChangeListener listener,
						ChangeType ct)
    {
	diddleQueue();
	Integer id = new Integer(feature_id);
	List listenerList = (List) featureListeners.get(id);
	if (listenerList == null) {
	    listenerList = new ArrayList();
	    featureListeners.put(id, listenerList);
	}
	listenerList.add(new ListenerMemento(ct, new FeatureListenerReference(id, listener, queue)));
    }

    public synchronized void removeFeatureListener(int feature_id,
						   ChangeListener listener,
						   ChangeType ct)
    {
	Integer id = new Integer(feature_id);
	List listenerList = (List) featureListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct == lm.type && listener.equals(lm.listener.get())) {
		    lm.listener.clear();
		    i.remove();
		    return;
		}
	    }
	}

	// Is this an error?
    }

    public void fireFeaturePreChange(ChangeEvent cev) 
        throws ChangeVetoException
    {
	BioSQLFeature source = (BioSQLFeature) cev.getSource();
	Integer id = new Integer(source._getInternalID());
	ChangeType ct = cev.getType();
	List listenerList = (List) featureListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct.isMatchingType(lm.type)) {
		    ChangeListener cl = (ChangeListener) lm.listener.get();
		    if (cl != null) {
			cl.preChange(cev);
		    }
		}
	    }
	}	
	
	FeatureHolder parent = source.getParent();
	ChangeEvent pcev = new ChangeEvent(parent, FeatureHolder.FEATURES, null, null, cev);
	if (parent instanceof Feature) {
	    fireFeaturePreChange(pcev);
	} else {
	    fireEntryPreChange(pcev);
	}
    }

    public void fireFeaturePostChange(ChangeEvent cev) 
    {
	BioSQLFeature source = (BioSQLFeature) cev.getSource();
	Integer id = new Integer(source._getInternalID());
	ChangeType ct = cev.getType();
	List listenerList = (List) featureListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct.isMatchingType(lm.type)) {
		    ChangeListener cl = (ChangeListener) lm.listener.get();
		    if (cl != null) {
			cl.postChange(cev);
		    }
		}
	    }
	}

	FeatureHolder parent = source.getParent();
	ChangeEvent pcev = new ChangeEvent(parent, FeatureHolder.FEATURES, null, null, cev);
	if (parent instanceof Feature) {
	    fireFeaturePostChange(pcev);
	} else {
	    fireEntryPostChange(pcev);
	}
    }

    public synchronized void addEntryListener(int entry_id,
					      ChangeListener listener,
					      ChangeType ct)
    {
	diddleQueue();
	Integer id = new Integer(entry_id);
	List listenerList = (List) entryListeners.get(id);
	if (listenerList == null) {
	    listenerList = new ArrayList();
	    entryListeners.put(id, listenerList);
	}
	listenerList.add(new ListenerMemento(ct, new EntryListenerReference(id, listener, queue)));
    }

    public synchronized void removeEntryListener(int entry_id,
						 ChangeListener listener,
						 ChangeType ct)
    {
	Integer id = new Integer(entry_id);
	List listenerList = (List) entryListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct == lm.type && listener.equals(lm.listener.get())) {
		    lm.listener.clear();
		    i.remove();
		    return;
		}
	    }
	}

	// Is this an error?
    }

    public void fireEntryPreChange(ChangeEvent cev) 
        throws ChangeVetoException
    {
	BioSQLSequenceI source = (BioSQLSequenceI) cev.getSource();
	Integer id = new Integer(source.getBioEntryID());
	ChangeType ct = cev.getType();
	List listenerList = (List) entryListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct.isMatchingType(lm.type)) {
		    ChangeListener cl = (ChangeListener) lm.listener.get();
		    if (cl != null) {
			cl.preChange(cev);
		    }
		}
	    }
	}

	ChangeEvent pcev = new ChangeEvent(seqDB, SequenceDB.SEQUENCES, null, null, cev);
	fireDatabasePreChange(pcev);
    }

    public void fireEntryPostChange(ChangeEvent cev) 
    {
	BioSQLSequenceI source = (BioSQLSequenceI) cev.getSource();
	Integer id = new Integer(source.getBioEntryID());
	ChangeType ct = cev.getType();
	List listenerList = (List) entryListeners.get(id);
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct.isMatchingType(lm.type)) {
		    ChangeListener cl = (ChangeListener) lm.listener.get();
		    if (cl != null) {
			cl.postChange(cev);
		    }
		}
	    }
	}

	ChangeEvent pcev = new ChangeEvent(seqDB, SequenceDB.SEQUENCES, null, null, cev);
	fireDatabasePostChange(pcev);
    }

    public synchronized void addDatabaseListener(ChangeListener listener,
						 ChangeType ct)
    {
	diddleQueue();
	databaseListeners.add(new ListenerMemento(ct, new WeakReference(listener, queue)));
    }

    public synchronized void removeDatabaseListener(ChangeListener listener,
						    ChangeType ct)
    {
	for (Iterator i = databaseListeners.iterator(); i.hasNext(); ) {
	    ListenerMemento lm = (ListenerMemento) i.next();
	    if (ct == lm.type && listener.equals(lm.listener.get())) {
		lm.listener.clear();
		i.remove();
		return;
	    }
	}
    }

    public void fireDatabasePreChange(ChangeEvent cev) 
        throws ChangeVetoException
    {
	ChangeType ct = cev.getType();
	List listenerList = databaseListeners;
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct.isMatchingType(lm.type)) {
		    ChangeListener cl = (ChangeListener) lm.listener.get();
		    if (cl != null) {
			cl.preChange(cev);
		    }
		}
	    }
	}
    }
    
    public void fireDatabasePostChange(ChangeEvent cev) 
    {
	ChangeType ct = cev.getType();
	List listenerList = databaseListeners;
	if (listenerList != null) {
	    for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
		ListenerMemento lm = (ListenerMemento) i.next();
		if (ct.isMatchingType(lm.type)) {
		    ChangeListener cl = (ChangeListener) lm.listener.get();
		    if (cl != null) {
			cl.postChange(cev);
		    }
		}
	    }
	}
    }

    private class FeatureAnnotationListenerReference extends WeakReference {
	private Object key;

	public FeatureAnnotationListenerReference(Object key, Object ref) {
	    super(ref);
	    this.key = key;
	}

	public FeatureAnnotationListenerReference(Object key, Object ref, ReferenceQueue queue) {
	    super(ref, queue);
	    this.key = key;
	}

	public Object getKey() {
	    return key;
	}
    }

    private class EntryAnnotationListenerReference extends WeakReference {
	private Object key;

	public EntryAnnotationListenerReference(Object key, Object ref) {
	    super(ref);
	    this.key = key;
	}

	public EntryAnnotationListenerReference(Object key, Object ref, ReferenceQueue queue) {
	    super(ref, queue);
	    this.key = key;
	}

	public Object getKey() {
	    return key;
	}
    }

    private class FeatureListenerReference extends WeakReference {
	private Object key;

	public FeatureListenerReference(Object key, Object ref) {
	    super(ref);
	    this.key = key;
	}

	public FeatureListenerReference(Object key, Object ref, ReferenceQueue queue) {
	    super(ref, queue);
	    this.key = key;
	}

	public Object getKey() {
	    return key;
	}
    }

    private class EntryListenerReference extends WeakReference {
	private Object key;

	public EntryListenerReference(Object key, Object ref) {
	    super(ref);
	    this.key = key;
	}

	public EntryListenerReference(Object key, Object ref, ReferenceQueue queue) {
	    super(ref, queue);
	    this.key = key;
	}

	public Object getKey() {
	    return key;
	}
    }

    private class ListenerMemento {
	public final ChangeType type;
	public final Reference listener;

	public ListenerMemento(ChangeType type, Reference listener) {
	    this.type = type;
	    this.listener = listener;
	}
    }
}
