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

/**
 * Annotation implementation which allows new key-value
 * pairs to be layered on top of an underlying Annotation.
 *
 * @author Thomas Down
 */

public class OverlayAnnotation implements Annotation {
    private Annotation parent;
    private Map overlay;

    public OverlayAnnotation(Annotation par) {
	parent = par;
	overlay = new HashMap();
    }

    public void setProperty(Object key, Object value) {
	overlay.put(key, value);
    }

    public Object getProperty(Object key) {
	Object val = overlay.get(key);
	if (val != null)
	    return val;
	return parent.getProperty(key);
    }

    public Set keys() {
	return new OAKeySet();
    }

    public Map asMap() {
	return new OAMap();
    }

    private class OAKeySet extends AbstractSet {
	private Set parentKeys;
     
	private OAKeySet() {
	    super();
	    parentKeys = parent.keys();
	}

	public Iterator iterator() {
	    return new Iterator() {
		Iterator oi = overlay.keySet().iterator();
		Iterator pi = parentKeys.iterator();
		Object peek = null;

		public boolean hasNext() {
		    if (peek == null)
			peek = nextObject();
		    return (peek != null);
		}

		public Object next() {
		    if (peek == null)
			peek = nextObject();
		    if (peek == null)
			throw new NoSuchElementException();
		    Object o = peek;
		    peek = null;
		    return o;
		}

		private Object nextObject() {
		    if (oi.hasNext())
			return oi.next();
		    Object po = null;
		    while (po == null && pi.hasNext()) {
			po = pi.next();
			if (overlay.containsKey(po))
			    po = null;
		    }
		    return po;
		}

		public void remove() {
		    throw new UnsupportedOperationException();
		}
	    } ;
	}

	public int size() {
	    int i = 0;
	    Iterator keys = iterator();
	    while(keys.hasNext()) {
		keys.next();
		++i;
	    }
	    return i;
	}

	public boolean contains(Object o) {
	    return overlay.containsKey(o) || parentKeys.contains(o);
	}
    }

    private class OAEntrySet extends AbstractSet {
	OAKeySet ks;

	private OAEntrySet() {
	    super();
	    ks = new OAKeySet();
	}

	public Iterator iterator() {
	    return new Iterator() {
		Iterator ksi = ks.iterator();

		public boolean hasNext() {
		    return ksi.hasNext();
		}

		public Object next() {
		    Object k = ksi.next();
		    Object v = getProperty(k);
		    return new OAMapEntry(k, v);
		}

		public void remove() {
		    throw new UnsupportedOperationException();
		}
	    } ;
	}

	public int size() {
	    return ks.size();
	}
    }

    private class OAMapEntry implements Map.Entry {
	private Object key;
	private Object value;

	private OAMapEntry(Object key, Object value) {
	    this.key = key;
	    this.value = value;
	}

	public Object getKey() {
	    return key;
	}

	public Object getValue() {
	    return value;
	}

	public Object setValue(Object v) {
	    throw new UnsupportedOperationException();
	}

	public boolean equals(Object o) {
	    if (! (o instanceof Map.Entry))
		return false;

	    Map.Entry mo = (Map.Entry) o;
	    return ((key == null ? mo.getKey() == null : key.equals(mo.getKey())) &&
		    (value == null ? mo.getValue() == null : value.equals(mo.getValue())));
	}

	public int hashCode() {
	    return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
	}
    }

    private class OAMap extends AbstractMap {
	OAEntrySet es;
	OAKeySet ks;

	private OAMap() {
	    super();
	    ks = new OAKeySet();
	    es = new OAEntrySet();
	}

	public Set entrySet() {
	    return es;
	}

	public Set keySet() {
	    return ks;
	}
	
	public Object get(Object key) {
	    try {
		return getProperty(key);
	    } catch (NoSuchElementException ex) {
	    }

	    return null;
	}
    }
}
