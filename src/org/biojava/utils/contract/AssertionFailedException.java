package org.biojava.utils.contract;

/**
 * Exception class that is thrown if an assertion failed.
 *
 * Since this class is a subclass of RuntimeException it is not enforced by the compiler to catch objects of this class.
 *
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A>
 */
public class AssertionFailedException extends RuntimeException {
  /**
   * construct with no particular message
   */
  public AssertionFailedException() {}

  /**
   * construct with the given message
   *
   * @param msg the message
   */
  public AssertionFailedException(String msg) {super(msg);}
}
