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

package org.acedb.staticobj;

import org.acedb.*;
import java.util.*;

public class StaticAceSet implements AceSet {
    private Map subSets;
    private AceSet parent;
    private String name;
    private AceURL url;

    public StaticAceSet(AceSet parent, AceURL url, String name) {
	this.parent = parent;
	this.url = url;
  this.name = name;
    }

    public int size() {
	if (subSets != null)
	    return subSets.size();
	return 0;
    }

    public AceURL toURL() {
	return url;
    }

    public String getName() {
      return name;
    }
    
    public AceSet getParent() {
	return parent;
    }

    public Iterator nameIterator() {
	if (subSets != null)
	    return subSets.keySet().iterator();
	return Collections.EMPTY_SET.iterator();
    }

    public Iterator iterator() {
	if (subSets != null)
	    return subSets.values().iterator();
	return Collections.EMPTY_SET.iterator();
    }

    public boolean contains(String name) {
	if (subSets != null)
	    return subSets.containsKey(name);
	return false;
    }

    public AceSet retrieve(String name) throws AceException {
      AceSet result = null;
      if (subSets != null) {
  	    result = (AceSet) subSets.get(name);
      
//          if(result == null) {
//            StringBuffer classes = new StringBuffer();
//            Iterator ni = subSets.keySet().iterator();
//            if(ni.hasNext()) {
//              Object obj = ni.next();
//              classes.append("\t'" + obj.toString() + "'=>'" + subSets.get(obj) + "'");
//              if(obj == name) {
//                classes.append(" == ");
//              }
//              if(obj.equals(name)) {
//                classes.append(" equals ");
//              }
//            }
//            while(ni.hasNext()) {
//              Object obj = ni.next();
//              classes.append(
//                "\n\t'" + obj.toString() + "'=>'" + subSets.get(obj) + "'" +
//                "\n\t'" + name + "'" + ", " + obj.getClass().getName()
//              );
//              if(obj == name) {
//                classes.append(" == ");
//              }
//              if(name.equals(obj)) {
//                classes.append(" equals ");
//              }
//            }
//            throw new AceException(
//              "Could not find child with the name '" +
//              name + "'" +
//              " and valid objects have the names:\n" +
//              classes.toString()
//            );
//          }
      }
      return result;
    }

    public void add(String name, AceSet set) {
	if (subSets == null)
	    subSets = new HashMap();
	subSets.put(name, set);
    }

    public AceSet filter(String expr) {
	throw new UnsupportedOperationException("ImplementMe");
    }
}
