/*
 * CrossReferenceResolutionException.java
 *
 * Created on September 1, 2005, 3:58 PM
 *
 */

package org.biojavax;

/**
 * An exception that indicates that an attempt to resolve a <code>CrossRef</code>
 * has failed.
 *
 * @author Mark Schreiber
 */
public class CrossReferenceResolutionException extends Exception{
    
    /** Creates a new instance of CrossReferenceResolutionException */
    public CrossReferenceResolutionException() {
        super();
    }
    
    /** 
     * Creates a new instance of CrossReferenceResolutionException with a
     * message.
     * @param message a description or reason for the exception.
     */
    public CrossReferenceResolutionException(String message) {
        super(message);
    }
    
    /** 
     * Creates a new instance of CrossReferenceResolutionException with a
     * message and a cause.
     * @param message a description or reason for the exception.
     * @param cause the exception that caused this one.
     */
    public CrossReferenceResolutionException(String message, Throwable cause){
        super(message, cause);
    }
    
    /** 
     * Creates a new instance of CrossReferenceResolutionException with a
     * cause.
     * @param cause the exception that caused this one.
     */
    public CrossReferenceResolutionException(Throwable cause) {
        super(cause);
    }
}
