/*                    BioJava development code
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

package org.biojava.bio.seq.impl;

import java.util.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.genomic.*;

/**
 * A no-frills implementation of Gene.
 *
 * @author Matthew Pocock
 * @since 1.1
 */
public class SimpleGene extends SimpleStrandedFeature implements Gene {
  private Set exons;
  
  {
    exons = new Set();
  }
  
  public Set getExons() {
    return Collections.unmodifiableSet(exons);
  }
  
  public SimpleGene(Gene.Template template) {
    super(template);
  }
}
