package org.biojava.utils.contract;

/**
 * Some minimal support for "design by contract" with pre-conditions, post-conditions and integrity rules (assertions).
 *
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A>
 */
public final class Contract {
  /**
   * flag that says if the checks are really performed. This should most probably always be true.
   */
  private static final boolean DO_IT = true;
  
  /**
   * private constructor - objects of this class can not be instantiated.
   */
  private Contract() {}

  /**
   * check if precondition is met and throw an exception if not.
   *
   * @param condition the precondition that must be met
   * @param msg       if the condition is not met (i.e. is not true) an object of class PreconditionNotMetException
   *                  (which is a sub-class of java.lang.IllegalArgumentException) is thrown with the message
   *                  "A precondition was not met: " + msg + ".".
   * @exception PreconditionNotMetException if condition is not true
   */
  public static void pre(boolean condition, String msg) {
    if (DO_IT && !condition) throw new PreconditionNotMetException("A precondition was not met: " + msg + ".");
  }

  /**
   * check if postcondition is met and throw an exception if not.
   *
   * @param condition the postcondition that must be met
   * @param msg       if the condition is not met (i.e. is not true) an object of class PostconditionNotMetException
   *                  (which is a sub-class of java.lang.RuntimeException) is thrown with the message
   *                  "A postcondition was not met: " + msg + ".".
   * @exception PostconditionNotMetException if condition is not true
   */
  public static void post(boolean condition, String msg) {
    if (DO_IT && !condition) throw new PostconditionNotMetException("A postcondition was not met: " + msg + ".");
  }

  /**
   * check if a condition is met and throw an exception if not.
   *
   * @param condition the condition that must be met
   * @param msg       if the condition is not met (i.e. is not true) an object of class AssertionFailedException
   *                  (which is a sub-class of java.lang.RuntimeException) is thrown with the message
   *                  "An assertion failed: " + msg + ".".
   * @exception AssertionFailedException if condition is not true
   */
  public static void assert(boolean condition, String msg) {
    if (DO_IT && !condition) throw new AssertionFailedException("An assertion failed: " + msg + ".");
  }
}
