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

  public AceNode parseObject(String obj) 
  throws AceException {
    obj = obj.trim();
    if (obj.startsWith("/")) {
	    throw new AceException("Empty object!");
    }

    PeekStringTokenizer toke = new PeekStringTokenizer(obj, "{},[] \t\n\r", true);
    if (! toke.nextToken().equals("{")) {
      throw new AceError("Unrecoverable parsing error: expecting {");
    }
    
    return /*(AceObject)*/ getNode(null, toke);
  }

  private StaticAceNode constructNode(
    StaticAceNode parent,
		String ty, 
		String va,
		String cl
  ) throws AceException {
    StaticAceNode obj = null;

//    System.out.println("parent " + parent + "\tty:" + ty + "\tva:" + va + "\tcl:" + cl);
    if (ty.equals("in")) {
	    obj = new StaticIntValue(Integer.parseInt(va), parent);
    } else if (ty.equals("fl")) {
      obj = new StaticFloatValue(Float.parseFloat(va), parent);
    } else if (ty.equals("dt")) {
      obj = new StaticDateValue(new Date(va), parent);
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
        obj = new StaticAceObject(
          va, 
		      Ace.fetch(parentDB.toURL().relativeURL(cl)),
          cl
        );
	    }
    } else if (ty.equals("tg")) {
	    obj = new StaticAceNode(va, parent);
    } else if (ty.equals("model-node")) {
      obj = new StaticModelNode(va, parent);
    } else if (ty.equals("model-reference")) {
      obj = new StaticModelReference(va, parent);
    } else if (ty.equals("model-include")) {
      obj = new StaticModelInclude(va, parent);
    } else if (ty.equals("model-type")) {
      obj = new StaticModelType(va, parent);
    } else {
      throw new AceError("Don't know how to handle type " + ty);
    }

    return obj;
  }
	

    private AceNode getNode(StaticAceNode parent, PeekStringTokenizer t) 
	throws AceException 
    {
	String ty = null;
	String va = null;
	String cl = null;
	StaticAceNode obj = null;

	while (true) {
   String s = t.nextToken();
   if(s.equals(",")) {
   } else if (s.startsWith("ty=>")) {
     if(ty != null) {
       throw new AceError("Resetting old ty value " + ty + " with " + s);
     }
     ty = s.substring(4).trim();
	 } else if (s.startsWith("va=>")) {
     if(va != null) {
		   throw new AceError("Resetting old va value " + va + " with " + s);
     }
     if(s.length() > 4) {
       va = s.substring(4);
     } else {
       va = t.nextToken();
     }
     va = Ace.encode(va);
   } else if (s.equals("cl=>")) {
     if(cl != null) {
       throw new AceError("Resetting old cl value " + cl + " with " + s);
     }
     if(s.length() > 4) {
       cl = s.substring(4);
     } else {
       cl = t.nextToken();
     }
     cl = Ace.encode(cl);
   } else if (s.equals("Pn=>")) {
		if (! t.nextToken().equals("[")) {
		    throw new AceError("Unrecoverable parsing error: expecting [");
		}
		if(ty == null) {
		    throw new AceError("Got null ty field for object");
		}
		obj = constructNode(parent, ty, va, cl);
		getChildren(obj, t);
		ty = null;
		va = null;
		cl = null;
	    } else /* { */ if (s.equals("}")) {
		if (obj != null) {
		    return obj;
		} else{ 
		    return constructNode(parent, ty, va, cl);
		}
      } else if (s.indexOf("=>") == 2) {
        System.out.println("Unknown tag in perl-style dump: " + s);
        if (s.length() == 4) {
          s = t.nextToken();
          System.out.println("Throwing away next token: " + s);
        }
      } else {
        if(ty != null || va != null) {
          throw new AceException(
            "Attempted to create model element with '" + s +
            "' when ty was " + ty + " and va was " + va
          );
        }
        String str = s.trim();
        if(str.startsWith("?")) {
          ty = "model-reference";
          str = str.substring(1);
        } else if(s.startsWith("#")) {
          ty = "model-include";
          str = str.substring(1);
        } else if(
          s.equals("Text") ||
          s.equals("DateType") ||
          s.equals("Int") ||
          s.equals("Float")
        ) {
          ty = "model-type";
        } else {
          ty = "model-node";
        }
        va = Ace.encode(str);
	    }
	}
    }

  private void getChildren(StaticAceNode parent, PeekStringTokenizer t) 
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



class PeekStringTokenizer {
    private StringTokenizer toke;
    private String cache = null;
    private static final String quote = "\01";

    public PeekStringTokenizer(String s) {
	toke = new StringTokenizer(s);
    }

    public PeekStringTokenizer(String s, String b) {
      this(s, b, false);
    }

    public PeekStringTokenizer(String s, String b, boolean r) {
      toke = new StringTokenizer(s, b+quote, r);
    }

    public int countTokens() {
	return toke.countTokens() + (cache == null ? 0 : 1);
    }

    public boolean hasMoreTokens() {
	return (cache == null ? toke.hasMoreTokens() : true);
    }

    public String nextToken() {
	if (cache == null) {
	    return extractToken();
	} else {
	    String t = cache;
	    cache = null;
	    return t;
	}
    }

    public String peekToken() {
	if (cache == null) {
	    cache = extractToken();
	}
	return cache;
    }
    
    private String extractToken() {
      String t = toke.nextToken();
      if(t.equals(quote)) {
        StringBuffer tb = new StringBuffer();
        for(String s = toke.nextToken(); !s.equals(quote); s = toke.nextToken()) {
          tb.append(s);
        }
        t = tb.toString();
      }
      return t;
    }
}
