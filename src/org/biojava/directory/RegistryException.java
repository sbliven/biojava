package org.biojava.directory;

import org.biojava.utils.NestedException;

/**
 * A <code>RegistryException</code> thrown when the registry cannot
 * find an implementation of a requested <code>SequenceDB</code>.
 *
 * @author Brian Gilman
 * @author Thomas Down
 * @author Keith James
 * @version $Revision$
 */
public class RegistryException extends NestedException {

    /**
     * Creates a new <code>RegistryException</code> with no detail
     * message.
     */
    public RegistryException(){
	super();
    }

    /**
     * Creates a new <code>RegistryException</code> with the specified
     * detail message.
     *
     * @param message a <code>String</code>.
     */
    public RegistryException(String message){
	super(message);
    }

    /**
     * Creates a new <code>RegistryException</code> with no detail
     * message, wrapping another <code>Throwable</code>.
     *
     * @param t a <code>Throwable</code>.
     */
    public RegistryException(Throwable t) {
	super(t);
    }

    /**
     * Creates a new <code>RegistryException</code> with the specified
     * detail message, wrapping another <code>Throwable</code>.
     *
     * @param t a <code>Throwable</code>.
     * @param message a <code>String</code>.
     */
    public RegistryException(Throwable t, String message) {
	super(t, message);
    }
}
