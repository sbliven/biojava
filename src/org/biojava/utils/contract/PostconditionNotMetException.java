package org.biojava.utils.contract;

/**
 * Exception class that is thrown if a postcondition of a method is not met.
 *
 * Since this class is a subclass of RuntimeException it is not enforced by the compiler to catch objects of this class.
 *
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A>
 */
public class PostconditionNotMetException extends RuntimeException {
  /**
   * construct with no particular message
   */
  public PostconditionNotMetException() {}

  /**
   * construct with the given message
   *
   * @param msg the message
   */
  public PostconditionNotMetException(String msg) {super(msg);}
}
