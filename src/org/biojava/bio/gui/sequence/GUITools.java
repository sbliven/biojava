package org.biojava.bio.gui.sequence;

import org.biojava.bio.symbol.RangeLocation;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 *
 * @author Matthew Pocock
 * @since 1.4
 */
public final class GUITools {
  private GUITools() {}

  public static RangeLocation getVisibleRange(SequenceRenderContext src, Graphics2D g2) {
    Rectangle2D clip = g2.getClipBounds();

    int min = Math.max(
            src.getRange().getMin(),
            src.graphicsToSequence(
                    new Point2D.Double(clip.getMinX(), clip.getMinY())
            ) - 1 );

    int max = Math.min(
            src.getRange().getMax(),
            src.graphicsToSequence(
                    new Point2D.Double(clip.getMaxX(), clip.getMaxY())
            ) + 1 );

    System.err.println("Originally: " + src.getRange());
    System.err.println("Now: " + min + ".." + max);

    return new RangeLocation(min, max);
  }

  public static Rectangle2D createOuterBounds(CircularRendererContext crc,
                                              double depth)
  {
    // do we need some extra data in crc to deal with origins not at 0?
    double outer = crc.getRadius() + depth;

    return new Rectangle2D.Double(-outer, -outer, 2.0 * outer, 2.0 * outer);
  }

  public static Rectangle2D createInnerBounds(CircularRendererContext crc) {
    // do we need some extra data in crc to deal with origins not at 0?
    double outer = crc.getRadius();

    return new Rectangle2D.Double(-outer, -outer, 2.0 * outer, 2.0 * outer);
  }
}
