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


package org.acedb;

import java.util.*;

/**
 * General utility methods for ACeDBC.
 *
 * @author Thomas Down
 */

public class AceUtils {
  public static AceNode pick(AceSet set) throws AceException {
    if (set.size() != 1) {
      StringBuffer msg = new StringBuffer("Set must have one member to be pickable: size=" +
                                          set.size());
      if(set instanceof AceNode) {
        AceNode an = (AceNode) set;
        msg.append(" " + an.getName());
        if(an.size() > 0) {
          msg.append("{");
          Iterator ci = an.nameIterator();
          msg.append(ci.next());
          while(ci.hasNext())
            msg.append("," + ci.next());
          msg.append("}");
        }
      }
	    throw new AceException(msg.toString());
    }
    Iterator i = set.iterator();
    return (AceNode) i.next();
  }

    public static AceSet retrieve(AceSet n, String sl) throws AceException {
	List l = toList(sl);
	return retrieve(n, l);
    }

    public static AceSet retrieve(AceSet n, List l) throws AceException {
	for (Iterator i = l.iterator(); i.hasNext(); ) {
	    String piece = (String) i.next();
	    n = n.retrieve(piece);
	    if (n == null)
		return null;
	}
	return n;
    }

    public static List toList(String sl) {
	List l = new ArrayList();

	for (StringTokenizer toke = new StringTokenizer(sl, "/");
	     toke.hasMoreTokens(); )
	{
	    l.add(toke.nextToken());
	}

	return l;
    }	    
}
