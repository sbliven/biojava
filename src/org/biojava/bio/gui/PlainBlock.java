package org.biojava.bio.gui;

import java.awt.*;
import java.awt.geom.*;

import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

public class PlainBlock implements BlockPainter {
  public void paintBlock(LogoContext ctxt, Rectangle2D block, AtomicSymbol sym) {
    Graphics2D g2 = ctxt.getGraphics();
    SymbolStyle style = ctxt.getStyle();
    
    try {
      g2.setPaint(style.fillPaint(sym));
    } catch (IllegalSymbolException ire) {
      g2.setPaint(Color.black);
    }
    g2.fill(block);
    
    try {
      g2.setPaint(style.outlinePaint(sym));
    } catch (IllegalSymbolException ire) {
      g2.setPaint(Color.gray);
    }
    g2.draw(block);
  }
}
