package org.biojava.directory;


/**
 * Class which gets thrown when the registry
 * cannot find an implementation of the sequenceDB
 *@author Brian Gilman
 *@version $Revision$
 */


public class RegistryException extends Exception{

    public RegistryException(){
	super();
    }

    public RegistryException(String message){
	super(message);
    }

}
