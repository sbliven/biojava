/**
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
 
package org.biojava.bio.seq.ragbag;

import java.util.HashMap;
import java.util.Map;
import java.lang.String;

import org.biojava.bio.seq.ComponentFeature;

/**
 * provides a mapping of a reference to a specific ComponentFeature
 * It exists primarily to support the getComponent method
 * of DASDataSource.
 *
 * @author David Huen
 * @since  1.2 
 */
public class RagbagHashedComponentDirectory implements RagbagComponentDirectory
{
  private Map componentDir;

  public RagbagHashedComponentDirectory()
  {
    componentDir = new HashMap();
  }

/**
 *  Add specified component feature to the directory
 */
  public void addComponentFeature(String ref, ComponentFeature cf)
  {
//    System.out.println("addComponentFeature " + ref);
    componentDir.put(ref, cf);
  }

/**
 * @param ref reference used in the RagbagMap file for this mapping
 */
  public ComponentFeature getComponentFeature(String ref)
  {
    return (ComponentFeature) componentDir.get(ref);
  }
}
