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
import java.beans.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.*;

import java.awt.*;
import java.awt.geom.*;

import java.util.List;

/**
 * A feature renderer that draws non-contiguous features as a set of boxes
 * joined by zig-zags.
 * <P>
 * This is aplicable to rendering cds's or non-contiguous homologies for
 * example.
 *
 * @author Matthew Pocock
 */
public class ZiggyFeatureRenderer extends AbstractFeatureRenderer
implements FeatureRenderer, java.io.Serializable {
  private Paint outline = Color.black;
  private Paint fill = Color.yellow;
  private double borderDepth = 3.0;


  public void setFill(Paint p) {
    Paint oldFill = fill;
    fill = p;
    pcs.firePropertyChange("fill", oldFill, fill);
  }

  public Paint getFill() {
    return fill;
  }

  public void setOutline(Paint p) {
    Paint oldOutline = outline;
    outline = p;
    pcs.firePropertyChange("outline", oldOutline, outline);
  }

  public Paint getOutline() {
    return outline;
  }
  
  public void setBorderDepth(double depth) {
    double oldDepth = borderDepth;
    borderDepth = depth;
    pcs.firePropertyChange(
      "borderDepth",
      new Double(oldDepth), new Double(depth)
    );
  }
  
  public double getBorderDepth() {
    return borderDepth;
  }
  
  public void renderFeature(
    Graphics2D g, Feature f, Rectangle2D box, SequenceRenderContext context
  ) {
    Location loc = f.getLocation();
    Iterator i = loc.blockIterator();
    Location last = null;
    if(i.hasNext()) {
      last = (Location) i.next();
      renderLocation(g, last, box, context);
    }
    while(i.hasNext()) {
      Location next = (Location) i.next();
      renderLink(g, f, last, next, box, context);
      renderLocation(g, next, box, context);
      last = next;
    }
  }
    
  private void renderLocation(
    Graphics2D g, Location loc, Rectangle2D box, SequenceRenderContext context
  ) {
    Rectangle2D.Double block = new Rectangle2D.Double();
    double min = context.sequenceToGraphics(loc.getMin());
    double max = context.sequenceToGraphics(loc.getMax()+1);
    if(context.getDirection() == context.HORIZONTAL) {
      block.setFrame(
        min, box.getMinY() + borderDepth,
        max - min, box.getHeight() - 2.0 * borderDepth
      );
    } else {
      block.setFrame(
        box.getMinX() + borderDepth, min,
        box.getHeight() - 2.0 * borderDepth, max - min
      );
    }
    g.setPaint(fill);
    g.fill(block);
    g.setPaint(outline);
    g.draw(block);
  }
    
  private void renderLink(
    Graphics2D g, Feature f, Location source, Location dest,
    Rectangle2D box, SequenceRenderContext context
  ) {
    Line2D line = new Line2D.Double();
    Point2D startP;
    Point2D midP;
    Point2D endP;
    if(context.getDirection() == context.HORIZONTAL) {
      if(
        (f instanceof StrandedFeature) &&
        (((StrandedFeature) f).getStrand() == StrandedFeature.NEGATIVE)
      ) {
        double start = context.sequenceToGraphics(dest.getMin());
        double end = context.sequenceToGraphics(source.getMax()+1);
        double mid = (start + end) * 0.5;
        startP = new Point2D.Double(start, box.getHeight() - borderDepth);
        midP   = new Point2D.Double(mid,   box.getHeight());
        endP   = new Point2D.Double(end,   box.getHeight() - borderDepth);
      } else {
        double start = context.sequenceToGraphics(source.getMax());
        double end = context.sequenceToGraphics(dest.getMin()+1);
        double mid = (start + end) * 0.5;
        startP = new Point2D.Double(start, borderDepth);
        midP   = new Point2D.Double(mid,   0);
        endP   = new Point2D.Double(end,   borderDepth);
      }
    } else {
      if(
        (f instanceof StrandedFeature) &&
        (((StrandedFeature) f).getStrand() == StrandedFeature.NEGATIVE)
      ) {
        double start = context.sequenceToGraphics(dest.getMin());
        double end = context.sequenceToGraphics(source.getMax()+1);
        double mid = (start + end) * 0.5;
        startP = new Point2D.Double(box.getHeight() - borderDepth, start);
        midP   = new Point2D.Double(box.getHeight(),               mid);
        endP   = new Point2D.Double(box.getHeight() - borderDepth, end);
      } else {
        double start = context.sequenceToGraphics(source.getMax());
        double end = context.sequenceToGraphics(dest.getMin()+1);
        double mid = (start + end) * 0.5;
        startP = new Point2D.Double(borderDepth, start);
        midP   = new Point2D.Double(0,           mid);
        endP   = new Point2D.Double(borderDepth, end);
      }
    }
    line.setLine(startP, midP);
    g.draw(line);
    line.setLine(midP, endP);
    g.draw(line);
  }
}
