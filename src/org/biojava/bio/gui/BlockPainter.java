package org.biojava.bio.gui;

import java.awt.geom.Rectangle2D;
import org.biojava.bio.symbol.AtomicSymbol;

public interface BlockPainter {
  public void paintBlock(LogoContext ctxt, Rectangle2D block, AtomicSymbol sym);
}
