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
 * Coppied from AceError By Thomas Down <td2@sanger.ac.uk>
 */

package org.biojava.bio;

import java.io.*;

public class BioError extends Error {
  private Throwable subException = null;

  public BioError(String message) {
	  super(message);
  }

  public BioError(Throwable ex) {
    this.subException = ex;
  }

  public BioError(Throwable ex, String message) {
    super(message);
    this.subException = ex;
  }
  
  public BioError() {
	  super();
  }

  public Throwable getWrappedException() {
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
