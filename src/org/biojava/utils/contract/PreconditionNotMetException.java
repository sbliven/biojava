package org.biojava.utils.contract;

/**
 * Exception class that is thrown if a precondition of a method is not met.
 *
 * Since this class is a subclass of java.lang.IllegalArgumentException it is not enforced by the compiler to catch
 * objects of this class.
 *
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A>
 */
public class PreconditionNotMetException extends IllegalArgumentException {
  /**
   * construct with no particular message
   */
  public PreconditionNotMetException() {}

  /**
   * construct with the given message
   *
   * @param msg the message
   */
  public PreconditionNotMetException(String msg) {super(msg);}
}
