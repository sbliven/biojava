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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.util.List;

import org.biojava.bio.symbol.SymbolList;

/**
 * <code>SymbolSequenceRenderer</code> renders symbols of a
 * <code>SymbolList</code>.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author David Huen
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public class SymbolSequenceRenderer implements SequenceRenderer
{
    private double depth = 25.0;
    private Paint  outline;

    public SymbolSequenceRenderer()
    {
        outline = Color.black;
    }

    public double getDepth(SequenceRenderContext context)
    {
        return depth + 1.0;
    }

    public double getMinimumLeader(SequenceRenderContext context)
    {
        return 0.0;
    }

    public double getMinimumTrailer(SequenceRenderContext context)
    {
        return 0.0;
    }

    public void paint(final Graphics2D g2, final SequenceRenderContext context)
    {
        Rectangle2D prevClip = g2.getClipBounds();
        AffineTransform prevTransform = g2.getTransform();

        g2.setPaint(outline);

        Font font = context.getFont();

        Rectangle2D maxCharBounds =
            font.getMaxCharBounds(g2.getFontRenderContext());

        double scale = context.getScale();

        if (scale >= (maxCharBounds.getWidth() * 0.3) &&
            scale >= (maxCharBounds.getHeight() * 0.3))
        {
            double xFontOffset = 0.0;
            double yFontOffset = 0.0;

            // These offsets are not set quite correctly yet. The
            // Rectangle2D from getMaxCharBounds() seems slightly
            // off. The "correct" application of translations based on
            // the Rectangle2D seem to give the wrong results. The
            // values below are mostly fudges.
            if (context.getDirection() == SequenceRenderContext.HORIZONTAL)
            {
                xFontOffset = maxCharBounds.getCenterX() * 0.25;
                yFontOffset = - maxCharBounds.getCenterY() + (depth * 0.5);
            }
            else
            {
                xFontOffset = - maxCharBounds.getCenterX() + (depth * 0.5);
                yFontOffset = - maxCharBounds.getCenterY() * 3.0;
            }

            int min = context.getRange().getMin();
            int max = context.getRange().getMax();
            SymbolList seq = context.getSymbols();

            for (int sPos = min; sPos <= max; sPos++)
            {
                double gPos = context.sequenceToGraphics(sPos);
                char c = seq.symbolAt(sPos).getToken();

                if (context.getDirection() == SequenceRenderContext.HORIZONTAL)
                {
                    g2.drawString(String.valueOf(c),
                                  (float) (gPos + xFontOffset),
                                  (float) yFontOffset);
                }
                else
                {
                    g2.drawString(String.valueOf(c),
                                  (float) xFontOffset,
                                  (float) (gPos + yFontOffset));
                }
            }
        }

        g2.setClip(prevClip);
        g2.setTransform(prevTransform);
    }

    public SequenceViewerEvent processMouseEvent(final SequenceRenderContext context,
                                                 final MouseEvent            me,
                                                 final List                  path)
    {
        path.add(this);
        int sPos = context.graphicsToSequence(me.getPoint());
        return new SequenceViewerEvent(this, null, sPos, me, path);
    }
}
