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
import java.io.*;
import java.lang.ref.*;

import org.acedb.staticobj.*;

/**
 * Parse the `Perl' representation of ACeDB objects.  Probably only
 * of interest to Driver developers.
 */

public class AcePerlParser {
  private Database parentDB;

  public AcePerlParser(Database db) {
    parentDB = db;
  }

  public AceObject parseObject(String obj) 
  throws AceException {
    obj = obj.trim();
    if (obj.startsWith("/")) {
	    throw new AceException("Empty object!");
    }

    StringTokenizer toke = new StringTokenizer(obj, "{},[] \t\n\r", true);
    if (! toke.nextToken().equals("{")) {
      throw new AceError("Unrecoverable parsing error: expecting {");
    }
    
    return (AceObject) getNode(null, toke);
  }

  private StaticAceNode constructNode(
    StaticAceNode parent,
		String ty, 
		String va,
		String cl
  ) throws AceException {
    StaticAceNode obj = null;

    if (ty.equals("in")) {
	    obj = new StaticIntValue(Integer.parseInt(va), parent);
    } else if (ty.equals("tx")) {
  	  obj = new StaticStringValue(va, parent);
    } else if (ty.equals("ob")) {
	    if (parent != null) {
        obj = new StaticReference(
          va,
          parent,
          Ace.rootURL(parent.toURL()).relativeURL(cl)
        );
	    } else {
        System.out.println("db url is " + parentDB.toURL());
        System.out.println("relative url is " + parentDB.toURL().relativeURL(cl));
        obj = new StaticAceObject(
          va, 
		      Ace.fetch(parentDB.toURL().relativeURL(cl)),
          cl
        );
	    }
    } else /* if (ty.equals("tg")) */ {
	    obj = new StaticAceNode(va, parent);
    }

    return obj;
  }
	

  private AceNode getNode(StaticAceNode parent, StringTokenizer t) 
  throws AceException {
    String ty = null;
    String va = null;
    String cl = null;
    StaticAceNode obj = null;

    while (true) {
	    String s = t.nextToken();
 
	    if (s.startsWith("ty=>")) {
        ty = s.substring(4).trim();
	    } else if (s.startsWith("va=>")) {
        va = Ace.encode(s.substring(4).trim());
	    } else if (s.startsWith("cl=>")) {
        cl = s.substring(4).trim();
	    } else if (s.equals("Pn=>")) {
        if (! t.nextToken().equals("[")) {
          throw new AceError("Unrecoverable parsing error: expecting [");
        }
        obj = constructNode(parent, ty, va, cl);
        getChildren(obj, t);
      } else /* { */ if (s.equals("}")) {
        if (obj != null) {
          return obj;
        } else{ 
          return constructNode(parent, ty, va, cl);
        }
	    }
    }
  }

  private void getChildren(StaticAceNode parent, StringTokenizer t) 
  throws AceException {
    while (true) {
      String s = t.nextToken();
      if (s.equals("{")) /* } */ {
        parent.addNode(getNode(parent, t));
      } else if (s.equals("]")) {
        return;
      }
    }
  }
}
