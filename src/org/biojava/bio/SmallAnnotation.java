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

package org.biojava.bio;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;

/**
 * Annotation that is optimized for memory usage.  Access time
 * is linear, so SmallAnnotations are not recommended when
 * the number of entries is large.  However, they are fine for
 * small numbers of keys.
 *
 * @author Thomas Down
 * @since 1.2
 */

public class SmallAnnotation implements Annotation {
    private Object[] mappings;
    private int numMappings = 0;
    protected transient ChangeSupport changeSupport = null;

    public SmallAnnotation() {
    }

    public SmallAnnotation(int size) {
	mappings = new Object[size * 2];
    }

    public Object getProperty(Object key) {
	int keyHash = key.hashCode();
	if (mappings != null) {
	    for (int i = 0; i < numMappings * 2; i += 2) {
		if (keyHash == mappings[i].hashCode() && key.equals(mappings[i])) {
		    return mappings[i + 1];
		}
	    }
	}

	throw new NoSuchElementException("Key " + key.toString() + " has no mapping");
    }

    public boolean containsProperty(Object key) {
	int keyHash = key.hashCode();
	if (mappings != null) {
	    for (int i = 0; i < numMappings * 2; i += 2) {
		if (keyHash == mappings[i].hashCode() && key.equals(mappings[i])) {
		    return true;
		}
	    }
	}

	return false;
    }

    public void setProperty(Object key, Object value)
        throws ChangeVetoException
    {
	if (changeSupport != null) {
	    Object oldValue = null;
	    if (containsProperty(key)) {
		oldValue = getProperty(key);
	    }
	    ChangeEvent ce = new ChangeEvent(
					     this,
					     Annotation.PROPERTY,
					     new Object[] { key, value },
					     new Object[] { key, oldValue}
					     );
	    synchronized (changeSupport) {
		changeSupport.firePreChangeEvent(ce);
		_setProperty(key, value);
		changeSupport.firePostChangeEvent(ce);
	    }
	} else {
	    _setProperty(key, value);
	}
    }
	
    private void _setProperty(Object key, Object value) {
	if (mappings == null) {
	    mappings = new Object[4];
	} else {
	    int keyHash = key.hashCode();

	    for (int i = 0; i < numMappings * 2; i += 2) {
		if (keyHash == mappings[i].hashCode() && key.equals(mappings[i])) {
		    mappings[i+1] = value;
		    return;
		}
	    }
	}

	int newPos = numMappings * 2;
	if (newPos + 1 >= mappings.length) {
	    Object[] newMappings = new Object[mappings.length + 6];
	    System.arraycopy(mappings, 0, newMappings, 0, mappings.length);
	    mappings = newMappings;
	}

	mappings[newPos] = key;
	mappings[newPos + 1] = value;
	numMappings++;
    }
    
    public Set keys() {
	return new KeySet();
    }

    public Map asMap() {
	return new EntryMap();
    }

    //
    // Changeable
    //

    public void addChangeListener(ChangeListener cl) {
	if(changeSupport == null) {
	    changeSupport = new ChangeSupport();
	}

	synchronized(changeSupport) {
	    changeSupport.addChangeListener(cl);
	}
    }

    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	if(changeSupport == null) {
	    changeSupport = new ChangeSupport();
	}

	synchronized(changeSupport) {
	    changeSupport.addChangeListener(cl, ct);
	}
    }

    public void removeChangeListener(ChangeListener cl) {
	if(changeSupport != null) {
	    synchronized(changeSupport) {
		changeSupport.removeChangeListener(cl);
	    }
	}
    }

    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	if(changeSupport != null) {
	    synchronized(changeSupport) {
		changeSupport.removeChangeListener(cl, ct);
	    }
	}
    }

    //
    // Support classes for our collection views
    //

    private class KeySet extends AbstractSet {
	public Iterator iterator() {
	    return new KeyIterator();
	}

	public int size() {
	    return numMappings;
	}

	public boolean contains(Object o) {
	    return containsProperty(o);
	}
    }

    private class KeyIterator implements Iterator {
	int pos = 0;
	
	public boolean hasNext() {
	    return pos < numMappings;
	}

	public Object next() {
	    if (pos >= numMappings) {
	        throw new NoSuchElementException();
	    }
	    int offset = pos * 2;
	    ++pos;
	    return mappings[offset];
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }

    private class EntryMap extends AbstractMap {
	public Set entrySet() {
	    return new EntrySet();
	}

	public Set keySet() {
	    return new KeySet();
	}

	public boolean containsKey(Object key) {
	    return containsProperty(key);
	}

	public Object get(Object key) {
	    if (containsProperty(key)) {
		return getProperty(key);
	    }

	    return null;
	}

	public int size() {
	    return numMappings;
	}
    }

    private class EntrySet extends AbstractSet {
	public Iterator iterator() {
	    return new EntryIterator();
	}

	public int size() {
	    return numMappings;
	}
    }

    private class EntryIterator implements Iterator {
	int pos = 0;
	
	public boolean hasNext() {
	    return pos < numMappings;
	}

	public Object next() {
	    if (pos >= numMappings) {
	        throw new NoSuchElementException();
	    }
	    int offset = pos * 2;
	    ++pos;
	    return new MapEntry(offset);
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }

    private class MapEntry implements Map.Entry {
	private int offset;

	private MapEntry(int offset) {
	    this.offset = offset;
	}

	public Object getKey() {
	    return mappings[offset];
	}

	public Object getValue() {
	    return mappings[offset + 1];
	}

	public Object setValue(Object v) {
	    throw new UnsupportedOperationException();
	}

	public boolean equals(Object o) {
	    if (! (o instanceof Map.Entry)) {
		return false;
	    }
	    
	    Map.Entry mo = (Map.Entry) o;
	    return ((getKey() == null ? mo.getKey() == null : getKey().equals(mo.getKey())) &&
		    (getValue() == null ? mo.getValue() == null : getValue().equals(mo.getValue())));
	}

	public int hashCode() {
	    return (getKey() == null ? 0 : getKey().hashCode()) ^ (getValue() == null ? 0 : getValue().hashCode());
	}
    }
}

