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
import java.net.*;

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
    
    /**
     * Get the root URL for a database.
     */

    public static AceURL rootURL(AceURL url) {
	String protocol = url.getProtocol();
	String userInfo = url.getUserInfo();
	String authority = url.getAuthority();
	String host = url.getHost();
	int port = url.getPort();
	return new AceURL(protocol, host, port, null, null, null, userInfo, authority);
    }
    
    public static String encode(String s) {
      s = URLEncoder.encode(s);
      if(s.indexOf("%") != -1 || s.indexOf("-") != -1) {
        StringBuffer sb = new StringBuffer(s);
        for(int i = sb.length()-1; i >= 0; i--) {
          if(sb.charAt(i) == '%') {
            sb.setCharAt(i, '-');
          } else if(sb.charAt(i) == '-') {
            sb.insert(i, '-');
          }
        }
        s = sb.toString();
      }
      return s;
    }
    
    public static String decode(String s) throws AceException {
//      System.out.println("decoding '" + s + "'");
      if(s.indexOf("-") != -1) {
        StringBuffer sb = new StringBuffer(s);
        for(int i = 0; i < sb.length(); i++) {
          if(sb.charAt(i) == '-') {
            if(sb.charAt(i+1) == '-') {
              sb.delete(i, i+1);
            } else {
              sb.setCharAt(i, '%');
            }
          }
        }
        s = sb.toString();
      }
      try {
        s = URLDecoder.decode(s);
      } catch (Exception e) {
        throw new AceException(e, "Couldn't decode " + s);
      }
//      System.out.println("decoded as '" + s + "'");
      return s;
    }
}
