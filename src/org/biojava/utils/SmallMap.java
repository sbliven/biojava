package org.biojava.utils;

import java.util.*;

/**
 * Lightweight implementation of Map which uses little memory to store a
 * small number of mappings, at the expense of scalability.  Not recommended
 * for more than 20-30 mappings.
 *
 * <p>
 * This implementation has the useful property that the iteration order is
 * the same as the order in which mappings are added.
 * </p>
 *
 * @author Thomas Down
 */

public class SmallMap extends AbstractMap {
    private Object[] mappings;
    private int numMappings = 0;

    public SmallMap() {
	this(5);
    }

    public SmallMap(int size) {
	super();
	mappings = new Object[size * 2];
    }

    public SmallMap(Map m) {
	this(m.size());
	for (Iterator i = m.entrySet().iterator(); i.hasNext(); ) {
	    Map.Entry me = (Map.Entry) i.next();
	    put(me.getKey(), me.getValue());
	}
    }

    /**
     * @throws NullPointerException if key is null
     */
    public Object get(Object key) {
	int keyHash = key.hashCode();
	for (int i = 0; i < numMappings * 2; i += 2) {
	    if (keyHash == mappings[i].hashCode() && key.equals(mappings[i])) {
		return mappings[i + 1];
	    }
	}
	return null;
    }

    /**
     * @throws NullPointerException if key is null
     */
    public Object put(Object key, Object value) {
	int keyHash = key.hashCode();
	
	for (int i = 0; i < numMappings * 2; i += 2) {
	    if (keyHash == mappings[i].hashCode() && key.equals(mappings[i])) {
		Object oldValue = mappings[i+1];
		mappings[i+1] = value;
		return oldValue;
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

	return null;
    }

    public Set keySet() {
	return new KeySet();
    }

    public Set entrySet() {
	return new EntrySet();
    }

    public int size() {
	return numMappings;
    }

    public boolean containsKey(Object key) {
	int keyHash = key.hashCode();
	for (int i = 0; i < numMappings * 2; i += 2) {
	    if (keyHash == mappings[i].hashCode() && key.equals(mappings[i])) {
		return true;
	    }
	}
	return false;
    }

    private void removeMapping(int num) {
	if (num < (numMappings - 1)) {
	    System.arraycopy(mappings, num + 1, mappings, num, (numMappings - num - 1) * 2);
	}
	numMappings--;
    }

    private class KeySet extends AbstractSet {
	public Iterator iterator() {
	    return new KeyIterator();
	}

	public int size() {
	    return numMappings;
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
	    removeMapping(pos);
	    pos -= 1;
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
	    removeMapping(pos);
	    pos -= 1;
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
	    Object oldValue = mappings[offset + 1];
	    mappings[offset + 1] = v;
	    return oldValue;
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
