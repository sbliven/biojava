package org.biojava.bibliography;

/**
 * An exception raised when communciation with the BibRef APIs fails.
 *
 * @author Matthew Pocock
 * @since 1.4
 */
public class BibRefException
extends Exception {
  public BibRefException(String message) {
    super(message);
  }
  
  public BibRefException(Throwable cause) {
    super(cause);
  }
  
  public BibRefException(String message, Throwable cause) {
    super(message, cause);
  }
}
