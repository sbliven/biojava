package org.biojava.bio.gui.sequence;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SequenceViewerSupport {
  private List listeners = new ArrayList();
  
  public void addSequenceViewerListener(SequenceViewerListener svl) {
    synchronized(listeners) {
      listeners.add(svl);
    }
  }
  
  public void removeSequenceViewerListener(SequenceViewerListener svl) {
    synchronized(listeners) {
      listeners.remove(svl);
    }
  }
  
  public void fireMouseClicked(SequenceViewerEvent sve) {
    List l;
    synchronized(listeners) {
      l = new ArrayList(listeners);
    }
    for(Iterator i = l.iterator(); i.hasNext(); ) {
      SequenceViewerListener svl = (SequenceViewerListener) i.next();
      svl.mouseClicked(sve);
    }
  }
  
  public void fireMousePressed(SequenceViewerEvent sve) {
    List l;
    synchronized(listeners) {
      l = new ArrayList(listeners);
    }
    for(Iterator i = l.iterator(); i.hasNext(); ) {
      SequenceViewerListener svl = (SequenceViewerListener) i.next();
      svl.mousePressed(sve);
    }
  }
  
  public void fireMouseReleased(SequenceViewerEvent sve) {
    List l;
    synchronized(listeners) {
      l = new ArrayList(listeners);
    }
    for(Iterator i = l.iterator(); i.hasNext(); ) {
      SequenceViewerListener svl = (SequenceViewerListener) i.next();
      svl.mouseReleased(sve);
    }
  }
}
