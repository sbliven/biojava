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


package org.biojava.bio.seq.io;

import java.util.NoSuchElementException;
import java.io.Serializable;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * Assumes that the description is of the form 'id\s*desc'.
 *
 * @author Matthew Pocock
 */
public class DefaultDescriptionReader
implements FastaDescriptionReader, Serializable {
  /**
   * Parses the description line, extracting the names for the URI and name fields.
   * <P>
   * The uri will be of the form <code>urn:sequence/fasta:id</code>. The name
   * will be null.
   */
  public String [] parseURNName(String desc) {
    java.util.StringTokenizer toc = new java.util.StringTokenizer(desc);
    String [] uriName = new String[2];
    
    String name = toc.nextToken().intern();
    uriName[0] = "uri:sequence/fasta:" + name;
    uriName[1] = name;
    
    return uriName;
  }
  
  /**
   * Parses annotation out from the description line.
   * <P>
   * This will set the id property to the id, and the description property
   * to everything following the first word.
   */
  public void parseAnnotation(String desc, Annotation annotation) {
    java.util.StringTokenizer toc = new java.util.StringTokenizer(desc);
    String id = toc.nextToken();
    try {
      if(id != null) {
        annotation.setProperty("id", id);
      }
      if(toc.hasMoreTokens()) {
        annotation.setProperty("description", toc.nextToken("******"));
      }
    } catch (ChangeVetoException cve) {
      throw new BioError(
        cve,
        "Couldn't parse decription as the annotation wouldn't let me add stuff"
      );
    }
  }

  public String writeDescription(Sequence seq) {
    String id = null;
    try {
      id = (String) seq.getAnnotation().getProperty("id");
    } catch (NoSuchElementException nsee) {
      id = seq.getName();
      if(id == null)
        id = seq.getURN();
    }
    String desc = null;
    try {
      desc = (String) seq.getAnnotation().getProperty("description");
    } catch (NoSuchElementException iae) {}
    
    if(desc == null) {
      return id;
    } else {
      return id + " " + desc;
    }
  }
}
