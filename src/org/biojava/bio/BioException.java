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
 * BioException.java
 *
 * Coppied from AceThrowable By Thomas Down <td2@sanger.ac.uk>
 */

package org.biojava.bio;

import java.io.*;

/**
 * A general perpose Exception that can wrap another exception.
 * <P>
 * It is common practice in BioJava to throw a BioException or a subclass of it
 * when something goes wrong. The exception can be used to catch another
 * throwable, thus keeping a complete record of where the original error
 * originated while adding annotation to the stack-trace. It also affords a neat
 * way to avoid exception-bloat on method calls, particularly when objects are
 * composed from several objects from different packages.
 *
 * @author Matthew Pocock
 */
public class BioException extends Exception {
  private Throwable subThrowable = null;

  public BioException(String message) {
	  super(message);
  }

  public BioException(Throwable ex) {
    this.subThrowable = ex;
  }

  public BioException(Throwable ex, String message) {
    super(message);
    this.subThrowable = ex;
  }
  
  public BioException() {
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
      String mes = sw1.toString();
      StringWriter sw2 = new StringWriter();
      super.printStackTrace(new PrintWriter( new PrintWriter(sw2)));
      String mes2 = sw2.toString();
      // count lines in mes2
      int lines = 0;
      int index = -1;
      while( (index = mes2.indexOf("\n", index)) > 0) {
        lines++;
        index++;
      }
      // trim mes
      index = mes.length();
      for(int i = 1; i < lines ; i++)
        index = mes.lastIndexOf("\n", index-1);
      pw.println(mes.substring(0, index));
      pw.print("rethrown as ");
      pw.print(mes2);
    } else {
      super.printStackTrace(pw);
    }
    pw.flush();
  }
}
