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


package org.acedb.staticobj;

import java.net.URLEncoder;
import org.acedb.*;

/**
 * @author Thomas Down
 */

public class StaticReference extends StaticAceNode
        implements Reference 
{
    private AceURL target;

    public StaticReference(String name, AceNode parent, AceURL classURL) {
      super(name, parent);
      this.target = classURL.relativeURL(URLEncoder.encode(name));
    }

    public AceURL getTarget() {
      return target;
    }
}
