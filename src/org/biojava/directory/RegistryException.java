package org.biojava.directory;

import org.biojava.utils.*;

/**
 * Class which gets thrown when the registry
 * cannot find an implementation of the sequenceDB
 *
 * @author Brian Gilman
 * @author Thomas Down
 * @version $Revision$
 */


public class RegistryException extends NestedException {

    public RegistryException(){
	super();
    }

    public RegistryException(String message){
	super(message);
    }

    public RegistryException(Throwable t) {
	super(t);
    }

    public RegistryException(Throwable t, String message) {
	super(t, message);
    }
}
