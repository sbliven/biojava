package org.biojava.bio.gui.sequence;

import java.util.*;
import java.awt.event.*;

/**
 * An event indicating that a mouse gesture was recognised within a widget that
 * renders sequences.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class SequenceViewerEvent extends EventObject {
  private final List path;
  private final Object target;
  private final MouseEvent mouseEvent;
  
  /**
   * Construct a SequenceViewerEvent with the given source, target, mouseEvent
   * and path.
   *
   * @param source  the event source, presumably a GUI component
   * @param target  an Object that is the target of the gesture - a feature, or
   *                {alignment, label, index} or some other structure
   * @param mouseEvent the MouseEvent that caused this event to be produced
   * @param path  a List of SequenceRenderer instances passed through to reach
   *              this event source
   */
  public SequenceViewerEvent(
    Object source,
    Object target,
    MouseEvent mouseEvent,
    List path
  ) {
    super(source);
    this.target = target;
    this.mouseEvent = mouseEvent;
    this.path = path;
  }
  
  /**
   * Get the list of SequenceRenderer instances that were passed through to
   * produce this event
   *
   * @returns a List of SequenceRenderer instances
   */
  public List getPath() {
    return path;
  }
  
  /**
   * Get the Object that was the target of the mouse gesture or null if the
   * mouse is not gesturing over any recognizable rendered object.
   *
   * @return the Object gestured at by the mouse event
   */
  public Object getTarget() {
    return target;
  }
  
  /**
   * Get the mouse event that caused this.
   *
   * @return the MouseEvent that caused this gesture to be noticed
   */
  public MouseEvent getMouseEvent() {
    return mouseEvent;
  }
}
