package org.biojava.bio.gui.sequence;

import java.util.*;

public interface SequenceViewerListener extends EventListener {
  void mouseClicked(SequenceViewerEvent sve);
  void mousePressed(SequenceViewerEvent sve);
  void mouseReleased(SequenceViewerEvent sve);
}
