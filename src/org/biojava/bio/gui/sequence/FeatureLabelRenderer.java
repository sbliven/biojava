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

/**
 * @author unknown
 * @author Matthew Pocock
 */
public class FeatureLabelRenderer
extends AbstractChangeable
implements FeatureRenderer {
  public static final ChangeType LABEL_MAKER = new ChangeType(
    "The label maker has changed",
    "org.biojava.bio.gui.sequence.FeatureLabelRenderer",
    "LABEL_MAKER",
    SequenceRenderContext.REPAINT
  );

  private static FontRenderContext FRC = new FontRenderContext(
    new AffineTransform(),
    false,
    true
  );

  private LabelMaker labelMaker;

  public FeatureLabelRenderer() {}

  public FeatureLabelRenderer(LabelMaker labelMaker) {
    try {
      setLabelMaker(labelMaker);
    } catch (ChangeVetoException cve) {
      throw new BioError(cve, "Assertion Failure: could not set label maker");
    }
  }

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
    return src.getFont().getMaxCharBounds(FRC).getHeight();
  }

  public double getMinimumLeader(SequenceRenderContext src) {
    return 0.0;
  }

  public double getMinimumTrailer(SequenceRenderContext src) {
    return 0.0;
  }

  public void renderFeature(
    Graphics2D g,
    Feature feat,
    SequenceRenderContext src
  ) {
    Location loc = feat.getLocation();
    String label = labelMaker.makeLabel(feat);

    System.err.println(
      "Feature: " + feat +
      " source: " + feat.getSource() +
      " type: " + feat.getType() +
      " loc: " + feat.getLocation() +
      " ann: " + feat.getAnnotation()
    );

    System.err.println("range: " + src.getRange());

    g.setPaint(Color.black);
    int min = Math.max(loc.getMin(), src.getRange().getMin());
    int max = Math.min(loc.getMax(), src.getRange().getMax());
    int mid = (min + max) / 2;

    System.err.println("min: " + min + " max: " + max + " mid: " + mid);
    g.drawString(
      label,
      (float) (src.sequenceToGraphics(mid)),
      (float) (getDepth(src) + 2.0)
    );
    System.err.println("rendered: " + label);
  }

  public FeatureHolder processMouseEvent(
    FeatureHolder hits,
    SequenceRenderContext src,
    MouseEvent me
  ) {
    return hits;
  }

  public static interface LabelMaker {
    String makeLabel(Feature f);
  }

  public static class SourceLabelMaker
  implements LabelMaker {
    public String makeLabel(Feature f) {
      return f.getSource();
    }
  }

  public static class TypeLabelMaker
  implements LabelMaker {
    public String makeLabel(Feature f) {
      return f.getType();
    }
  }

  public static class AnnotationLabelMaker
  implements LabelMaker {
    private Object key;

    public AnnotationLabelMaker() {
    }

    public AnnotationLabelMaker(Object key) {
      setKey(key);
    }

    public void setKey(Object key) {
      this.key = key;
    }

    public Object getKey() {
      return key;
    }

    public String makeLabel(Feature feat) {
      Annotation ann = feat.getAnnotation();
      if(ann.containsProperty(key)) {
        return ann.getProperty(key).toString();
      } else {
        return "";
      }
    }
  }
}
