package org.biojava.bio.gui.sequence;

import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.seq.FeatureHolder;

import javax.swing.*;
import java.awt.*;

/**
 * Renders a sequence as a circle using a CircularRenderer.
 *
 * @author Matthew Pocock
 * @since 1.4
 */
public class CircularRendererPanel
extends JComponent {
  private final CircularRendererContext ctxt;

  {
    ctxt = new CTXT();
  }

  private SymbolList symList;
  private double radius;
  private CircularRenderer renderer;
  private double offset;

  public double getRadius() {
    return radius;
  }

  public void setRadius(double radius) {
    this.radius = radius;
  }

  public SymbolList getSequence() {
    return symList;
  }

  public void setSequence(SymbolList symList) {
    this.symList = symList;
  }

  public double getOffset() {
    return offset;
  }

  public void setOffset(double offset) {
    this.offset = offset;
  }

  public CircularRenderer getRenderer() {
    return renderer;
  }

  public void setRenderer(CircularRenderer renderer) {
    this.renderer = renderer;
  }

  public synchronized void paintComponent(Graphics g) {
    super.paintComponent(g);
    if(!isActive()) return;

    Graphics2D g2 = (Graphics2D) g;

    renderer.paint(g2, ctxt);
  }

  private boolean isActive() {
    return renderer != null;
  }

  private final class CTXT
  implements CircularRendererContext {
    public double getOffset() {
      return offset;
    }

    public double getAngle(int indx) {
      return ((double) indx) * 2.0 * Math.PI / ((double) symList.length());
    }

    public int getIndex(double angle) {
      return (int) (angle * ((double) symList.length()) / (2.0 * Math.PI));
    }

    public double getRadius() {
      return radius;
    }

    public SymbolList getSymbols() {
      return symList;
    }

    public FeatureHolder getFeatures() {
      if(symList instanceof FeatureHolder) {
        return (FeatureHolder) symList;
      } else {
        return FeatureHolder.EMPTY_FEATURE_HOLDER;
      }
    }
  }
}
