package org.biojava.bio.gui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;

import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

public class TextBlock implements BlockPainter {
  private Font logoFont = new Font("Tahoma", Font.PLAIN, 12);
  
  /**
   * Retrieve the current font.
   *
   * @return the current logo font
   */
  public Font getLogoFont() {
    return logoFont;
  }
  
  /**
   * Set the current logo font.
   *
   * @param logoFont the new Font to render the logo letters in
   */
  public void setLogoFont(Font logoFont) {
    this.logoFont = logoFont;
  }

  public void paintBlock(LogoContext ctxt, Rectangle2D block, AtomicSymbol sym) {
    Graphics2D g2 = ctxt.getGraphics();
    SymbolStyle style = ctxt.getStyle();
    
    FontRenderContext frc = g2.getFontRenderContext();
    GlyphVector gv = logoFont.createGlyphVector(frc, sym.getToken() + "");
    Shape outline = gv.getOutline();
    Rectangle2D oBounds = outline.getBounds2D();
    
    AffineTransform at = new AffineTransform();
    at.setToTranslation(block.getX(), block.getY());
    at.scale(
      block.getWidth() / oBounds.getWidth(),
      block.getHeight() / oBounds.getHeight()
    );
    at.translate(-oBounds.getMinX(), -oBounds.getMinY());
    outline = at.createTransformedShape(outline);
    
    try {
      g2.setPaint(style.fillPaint(sym));
    } catch (IllegalSymbolException ire) {
      g2.setPaint(Color.black);
    }
    g2.fill(outline);
    
    try {
      g2.setPaint(style.outlinePaint(sym));
    } catch (IllegalSymbolException ire) {
      g2.setPaint(Color.gray);
    }
    g2.draw(outline);
  }
}
