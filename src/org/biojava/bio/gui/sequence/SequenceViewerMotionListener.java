package org.biojava.bio.gui.sequence;

import java.util.*;

public interface SequenceViewerMotionListener
extends EventListener {
  void mouseDragged(SequenceViewerEvent sve);
  void mouseMoved(SequenceViewerEvent sve);
}
