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
 * NestedException.java
 *
 * Coppied from AceThrowable By Thomas Down <td2@sanger.ac.uk>
 */

package org.biojava.utils;

import java.io.*;
import java.util.*;

/**
 * A general perpose Exception that can wrap another exception.
 * <P>
 * It is common practice in BioJava to throw a NestedException or a subclass of it
 * when something goes wrong. The exception can be used to catch another
 * throwable, thus keeping a complete record of where the original error
 * originated while adding annotation to the stack-trace. It also affords a neat
 * way to avoid exception-bloat on method calls, particularly when objects are
 * composed from several objects from different packages.
 *
 * @author Matthew Pocock
 */
public class NestedException extends Exception {
  private Throwable subThrowable = null;

  public NestedException(String message) {
	  super(message);
  }

  public NestedException(Throwable ex) {
    this.subThrowable = ex;
  }

  public NestedException(Throwable ex, String message) {
    super(message);
    this.subThrowable = ex;
  }
  
  public NestedException() {
	  super();
  }

  public void printStackTrace() {	
    printStackTrace(System.err);
  }
  
  public void printStackTrace(PrintStream ps) {
    printStackTrace(new PrintWriter(ps));
  }
  
  public void printStackTrace(PrintWriter pw) {
  	if (subThrowable != null) {
      StringWriter sw1 = new StringWriter();
	    subThrowable.printStackTrace(new PrintWriter(sw1));
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
