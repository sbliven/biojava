package org.biojava.bio.gui.sequence;

import org.biojava.bio.seq.Feature;

import java.awt.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public interface CircularFeatureRenderer {
  public void renderFeature(
    Graphics2D g,
    Feature f,
    CircularRendererContext context );

  public double getDepth(CircularRendererContext crc);
}
