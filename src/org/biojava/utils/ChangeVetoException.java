/*
 * BioJava development code
 * 
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 * 
 * http://www.gnu.org/copyleft/lesser.html
 * 
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 * 
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 * 
 * http://www.biojava.org
 */

package org.biojava.utils;

/**
 *  
 * Exception which is thrown when a ChangeListener does not
 * wish a change to take place.
 *
 * @author     Thomas Down
 * @author     Matthew Pocock
 * @since      1.1 
 */

public class ChangeVetoException extends NestedException {
  private final ChangeEvent change;

  public ChangeVetoException() {
    super();
    change = null;
  }
  
  /**
   *  Construct an exception to veto a change without explanation. 
   *
   * @param  change  An event which is being vetoed. 
   */

  public ChangeVetoException(ChangeEvent change) {
    super();
    this.change = change;
  }

  /**
   *  Create an exception with a detail message 
   *
   * @param  reason  A detail message. 
   */

  public ChangeVetoException(
    String reason
  ) {
    super(reason);
    this.change = null;
  }

  /**
   *  Construct an exception to veto a change for a specified reason. 
   *
   * @param  change  An event which is being vetoed. 
   * @param  reason  A detail message. 
   */

  public ChangeVetoException(ChangeEvent change, String reason) {
    super(reason);
    this.change = change;
  }

  /**
   *  Propogate an exception without (additional) explanation. 
   *
   * @param  ex      A parent exception 
   * @param  change  An event which is being vetoed. 
   */

  public ChangeVetoException(Exception ex, ChangeEvent change) {
    super(ex);
    this.change = change;
  }
  
  /**
   *  Propogate an exception, giving a detail message 
   *
   * @param  ex      A parent exception 
   * @param  reason  A detail message. 
   */

   public ChangeVetoException(Exception ex, String reason) {
    super(ex, reason);
    this.change = null;
  }

  /**
   *  Propogate an exception, giving a detail message 
   *
   * @param  ex      A parent exception 
   * @param  change  An event which is being vetoed. 
   * @param  reason  A detail message. 
   */

  public ChangeVetoException(
    Exception ex, 
    ChangeEvent change, 
    String reason
  ) {
    super(ex, reason);
    this.change = change;
  }

  /**
   *  Return the ChangeEvent which is being vetoed. 
   *
   * @return    The ChangeEvent value 
   */

  public ChangeEvent getChangeEvent() {
    return change;
  }
}

