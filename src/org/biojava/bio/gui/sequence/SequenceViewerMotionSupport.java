package org.biojava.bio.gui.sequence;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SequenceViewerMotionSupport {
  private List listeners = new ArrayList();
  
  public void addSequenceViewerMotionListener(SequenceViewerMotionListener svl) {
    synchronized(listeners) {
      listeners.add(svl);
    }
  }
  
  public void removeSequenceViewerMotionListener(SequenceViewerMotionListener svl) {
    synchronized(listeners) {
      listeners.remove(svl);
    }
  }
  
  public void fireMouseDragged(SequenceViewerEvent sve) {
    List l;
    synchronized(listeners) {
      l = new ArrayList(listeners);
    }
    for(Iterator i = l.iterator(); i.hasNext(); ) {
      SequenceViewerMotionListener svml = (SequenceViewerMotionListener) i.next();
      svml.mouseDragged(sve);
    }
  }
  
  public void fireMouseMoved(SequenceViewerEvent sve) {
    List l;
    synchronized(listeners) {
      l = new ArrayList(listeners);
    }
    for(Iterator i = l.iterator(); i.hasNext(); ) {
      SequenceViewerMotionListener svml = (SequenceViewerMotionListener) i.next();
      svml.mouseMoved(sve);
    }
  }
}
