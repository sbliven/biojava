package org.biojava.bio.gui;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import org.biojava.bio.dist.Distribution;

public interface LogoContext {
  public Graphics2D getGraphics();
  public Distribution getDistribution();
  public SymbolStyle getStyle();
  public Rectangle getBounds();
  public BlockPainter getBlockPainter();
}
