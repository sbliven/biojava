package org.biojava.bio.gui.sequence;

import org.biojava.utils.*;

public abstract class AbstractForwarder
extends AbstractChangeable {
  protected transient SequenceRenderContext.LayoutForwarder layoutF;
  protected transient SequenceRenderContext.RepaintForwarder repaintF;
  
  private ChangeListener logger =
    new ChangeListener.LoggingListener(System.out);
  {
    layoutF = new SequenceRenderContext.LayoutForwarder(this, getChangeSupport(
      SequenceRenderContext.LAYOUT
    ));
    repaintF = new SequenceRenderContext.RepaintForwarder(this, getChangeSupport(
      SequenceRenderContext.REPAINT
    ));
  }
  
  protected void registerLayout(Object o, ChangeType ct) {
    if(o instanceof Changeable) {
      ((Changeable) o).addChangeListener(layoutF, ct);
    }
  }
  
  protected void unregisterLayout(Object o, ChangeType ct) {
    if(o instanceof Changeable) {
      ((Changeable) o).removeChangeListener(layoutF, ct);
    }
  }
  
  protected void registerRepaint(Object o, ChangeType ct) {
    if(o instanceof Changeable) {
      ((Changeable) o).addChangeListener(repaintF, ct);
    }
  }
  
  protected void unregisterRepaint(Object o, ChangeType ct) {
    if(o instanceof Changeable) {
      ((Changeable) o).removeChangeListener(repaintF, ct);
    }
  }
}
