package org.biojava.bio.gui.sequence;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.program.abi.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

import java.util.List;

public class AbiTraceRenderer
extends AbstractChangeable
implements SequenceRenderer {
  public static final ChangeType TRACE = new ChangeType(
    "The trace has changed",
    AbiTraceRenderer.class,
    "TRACE",
    SequenceRenderContext.LAYOUT
  );

  public static final ChangeType DEPTH = new ChangeType(
    "The trace render depth has changed",
    AbiTraceRenderer.class,
    "DEPTH",
    SequenceRenderContext.LAYOUT
  );

  private ABITrace trace;
  private double depth;

  public AbiTraceRenderer() {
  }

  public void paint(Graphics2D g, SequenceRenderContext ctxt) {
    if(ctxt.getDirection() == SequenceRenderContext.VERTICAL || trace == null) {
      return;
    }

    try {
      Rectangle2D clip = g.getClip().getBounds2D();

      int min = Math.max(ctxt.getRange().getMin(), ctxt.graphicsToSequence(clip.getMinX()));
      int max = Math.min(ctxt.getRange().getMax(), ctxt.graphicsToSequence(clip.getMaxX()));;
      int[] baseCalls = trace.getBasecalls();
      int[] traceA = trace.getTrace(DNATools.a());
      int[] traceG = trace.getTrace(DNATools.g());
      int[] traceC = trace.getTrace(DNATools.c());
      int[] traceT = trace.getTrace(DNATools.t());

      g.setColor(Color.green);
      renderTrace(baseCalls, traceA, g, ctxt, min, max);
      g.setColor(Color.black);
      renderTrace(baseCalls, traceG, g, ctxt, min, max);
      g.setColor(Color.blue);
      renderTrace(baseCalls, traceC, g, ctxt, min, max);
      g.setColor(Color.red);
      renderTrace(baseCalls, traceT, g, ctxt, min, max);
    } catch (IllegalSymbolException ise) {
      throw new BioError(ise, "Can't process trace file");
    }
  }

  private void renderTrace(int[] baseCalls, int[] trace, Graphics2D g, SequenceRenderContext ctxt, int min, int max) {
    double depthScale = depth / 2000.0; // assume X gredations
    Line2D line = new Line2D.Float();
    for(int pos = min; pos <= max; pos++) {
      int minT;
      int maxT;

      if(pos == 1) {
        minT = 0;
      } else {
        minT = (baseCalls[pos - 2] + baseCalls[pos - 1]) / 2;
      }

      if(pos == baseCalls.length) {
        maxT = trace.length - 1;
      } else {
        maxT = (baseCalls[pos - 1] + baseCalls[pos - 0]) / 2;
      }

      double scale = ctxt.getScale() / (double) (maxT - minT);

      double stg = ctxt.sequenceToGraphics(pos);
      for(int i = minT; i < maxT; i++) {
        int j = i - minT;
        line.setLine(
          stg + scale * (0.5 + j),
          depth - trace[i] * depthScale,
          stg + scale * (0.5 + j + 1),
          depth - trace[i + 1] * depthScale
        );

        g.draw(line);
      }
    }
  }

  public void setTrace(ABITrace trace)
  throws ChangeVetoException {
    ChangeSupport cs = getChangeSupport(TRACE);
    synchronized(cs) {
      ChangeEvent ce = new ChangeEvent(this, TRACE, trace, this.trace);
      cs.firePreChangeEvent(ce);
      this.trace = trace;
      cs.firePostChangeEvent(ce);
    }
  }

  public ABITrace getTrace() {
    return trace;
  }

  public void setDepth(double depth)
  throws ChangeVetoException {
    if(depth < 0.0) {
      throw new ChangeVetoException("Can't set depth to a negative number: " + depth);
    }

    ChangeSupport cs = getChangeSupport(DEPTH);
    synchronized(cs) {
      ChangeEvent ce = new ChangeEvent(this, DEPTH, new Double(depth), new Double(this.depth));
      cs.firePreChangeEvent(ce);
      this.depth = depth;
      cs.firePostChangeEvent(ce);
    }
  }

  public double getDepth(SequenceRenderContext src) {
    if(src.getDirection() == SequenceRenderContext.HORIZONTAL && trace != null) {
      return depth;
    } else {
      return 0.0;
    }
  }

  public double getMinimumLeader(SequenceRenderContext src) {
    return 0.0;
  }

  public double getMinimumTrailer(SequenceRenderContext src) {
    return 0.0;
  }

  public SequenceViewerEvent processMouseEvent(
    SequenceRenderContext src,
    MouseEvent me,
    List path
  ) {
    // don't do anything
    return null;
  }
}

