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

import org.biojava.utils.NestedException;

/**
 * @author Thomas Down
 */

public class AceException extends NestedException {
    private boolean recoverable = false;

    public AceException(String message) {
  	super(message);
    }

    public AceException(String message, boolean rec) {
  	super(message);
	this.recoverable = rec;
    }

    public AceException(Throwable ex) {
	super(ex);
    }

    public AceException(Throwable ex, String message) {
	super(ex, message);
    }
    
    public AceException() {
	super();
    }

    public boolean isRecoverable() {
	return recoverable;
    }
}
