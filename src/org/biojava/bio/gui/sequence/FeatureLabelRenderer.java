/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.gui.sequence;

import java.util.*;
import java.awt.event.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.gui.*;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import java.util.List;

public class FeatureLabelRenderer
extends AbstractChangeable
implements SequenceRenderer {
  public static final ChangeType LABEL_MAKER = new ChangeType(
    "The label maker has changed",
    "org.biojava.bio.gui.sequence.FeatureLabelRenderer",
    "FILL",
    SequenceRenderContext.LAYOUT
  );
  
  private final double depth = 20;
  private LabelMaker labelMaker;
  
  public LabelMaker getLabelMaker() {
    return this.labelMaker;
  }
  
  public void setLabelMaker(LabelMaker labelMaker)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(LABEL_MAKER);
      ChangeEvent ce = new ChangeEvent(
        this, LABEL_MAKER,
        labelMaker, this.labelMaker
      );
      synchronized(cs) {
        cs.firePreChangeEvent(ce);
        this.labelMaker = labelMaker;
        cs.firePostChangeEvent(ce);
      }
    } else {
      this.labelMaker = labelMaker;
    }
  }
  
  public double getDepth(SequenceRenderContext src) {
    List layers = bumpLabels(src);
    return layers.size() * depth + 1.0;
  }
  
  public double getMinimumLeader(SequenceRenderContext src) {
    return 0.0;
  }
  
  public double getMinimumTrailer(SequenceRenderContext src) {
    return 0.0;
  }
  
  public void paint(
    Graphics2D g, SequenceRenderContext src
  ) {
    List layers = bumpLabels(src);
    
  }
  
  public SequenceViewerEvent processMouseEvent(
    SequenceRenderContext src,
    MouseEvent me,
    List path
  ) {
    path.add(this);
    int sPos = src.graphicsToSequence(me.getPoint());
    return new SequenceViewerEvent(this, null, sPos, me, path);
  }
  
  protected List bumpLabels(SequenceRenderContext src) {
    List lines = new ArrayList();
    
    for(
      Iterator fi = ((FeatureHolder) src.getSymbols()).filter(
        new FeatureFilter.OverlapsLocation(src.getRange()),
        false
      ).features();
      fi.hasNext();
    ) {
      Feature f = (Feature) fi.next();
      String label = labelMaker.makeLabel(f);
      
      Location fLoc = null; ///
      FeatLocLabel featLocLabel = new FeatLocLabel(f, fLoc, label);
      
      Iterator lineI = lines.iterator();
      List line = null;
    LAYER:
      while(lineI.hasNext()) {
        List curLine = (List) lineI.next();
        Iterator curLineI = curLine.iterator();
        while(curLineI.hasNext()) {
          Location curLoc = ((FeatLocLabel) curLineI.next()).getLocation();
          if(curLoc.overlaps(fLoc)) {
            continue LAYER;
          }
        }
        line = curLine;
        break;
      }
      if(line == null) {
        lines.add(line = new ArrayList());
      }
      line.add(featLocLabel);
    }
    return lines;
  }
  
  final static private class FeatLocLabel {
    private final Feature f;
    private final Location location;
    private final String label;
    
    public final Feature getFeature() {
      return f;
    }
    
    public final Location getLocation() {
      return location;
    }
    
    public final String getLabel() {
      return label;
    }
    
    public FeatLocLabel(Feature f, Location loc, String label) {
      this.f = f;
      this.location = loc;
      this.label = label;
    }
    
    public String toString() {
      return "feature: " + f + " location: " + location + " label: " + label;
    }
  }
  
  public static interface LabelMaker {
    String makeLabel(Feature f);
  }
}
