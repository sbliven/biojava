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


/*
 * NestedError.java
 *
 * Coppied from AceError By Thomas Down <td2@sanger.ac.uk>
 */

package org.biojava.utils;

import java.io.*;
import java.util.*;

/**
 * A general purpose Error that can wrap another Throwable object.
 * <p>
 * NestedError is an Error that should be thrown whenever some exceptional and
 * unforseable event takes place. For example, sometimes exceptions can be
 * thrown by a given method, but not when the calling method is a member of
 * the same class. In this case, the try-catch block would collect the
 * 'impossible' exception and throw a NestedError that wraps it.
 *
 * @author Matthew Pocock
 */
public class NestedError extends Error {
  /**
   * The wrapped Throwable object
   */
  private final Throwable subException;

  public NestedError(String message) {
	  this(null, message);
  }

  public NestedError(Throwable ex) {
    this(ex, null);
  }

  public NestedError(Throwable ex, String message) {
    super(message);
    this.subException = ex;
  }
  
  public NestedError() {
	  this(null, null);
  }

  public Throwable getWrappedException() {
    /**
    *sends a Throwable object to standard out if encountered in the stack trace or returns null.
    */
    return subException;
  }
  
  public void printStackTrace() {	
    printStackTrace(System.err);
  }
  
  public void printStackTrace(PrintStream ps) {
    printStackTrace(new PrintWriter(ps));
  }
  
  public void printStackTrace(PrintWriter pw) {
    if (subException != null) {
      StringWriter sw1 = new StringWriter();
      subException.printStackTrace(new PrintWriter(sw1));
      String mes1 = sw1.toString();
      StringWriter sw2 = new StringWriter();
      super.printStackTrace(new PrintWriter(sw2));
      String mes2 = sw2.toString();

      try {
        List lines1 = lineSplit(new BufferedReader(new StringReader(mes1)));
        List lines2 = lineSplit(new BufferedReader(new StringReader(mes2)));
      
        ListIterator li1 = lines1.listIterator(lines1.size());
        ListIterator li2 = lines2.listIterator(lines2.size());
      
        while(li1.hasPrevious() && li2.hasPrevious()) {
          Object s1 = li1.previous();
          Object s2 = li2.previous();
          
          if(s1.equals(s2)) {
            li1.remove();
          } else {
            break;
          }
        }
        for(Iterator i = lines1.iterator(); i.hasNext(); ) {
          pw.println(i.next());
        }
        pw.print("rethrown as ");
        pw.print(mes2);
      } catch (IOException ioe) {
        throw new Error("Coudn't merge stack-traces");
      }
    } else {
      super.printStackTrace(pw);
    }
    pw.flush();
  }
  
  private List lineSplit(BufferedReader in) throws IOException {
    List lines = new ArrayList();
    for(String line = in.readLine(); line != null; line = in.readLine()) {
      lines.add(line);
    }
    return lines;
  }
}
