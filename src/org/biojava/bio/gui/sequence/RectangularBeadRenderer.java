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

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

/**
 * <code>RectangularBeadRenderer</code> renders features as
 * simple rectangles. Their outline and fill <code>Paint</code>,
 * <code>Stroke</code>, feature depth, Y-axis displacement are
 * configurable.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class RectangularBeadRenderer extends AbstractBeadRenderer
    implements FeatureRenderer
{
    protected Rectangle2D rect;

    /**
     * Creates a new <code>RectangularBeadRenderer</code>.
     *
     * @param beadDepth a <code>double</code>.
     * @param beadDisplacement a <code>double</code>.
     * @param beadOutline a <code>Paint</code>.
     * @param beadFill a <code>Paint</code>.
     * @param beadStroke a <code>Stroke</code>.
     */
    public RectangularBeadRenderer(final double beadDepth,
                                   final double beadDisplacement,
                                   final Paint  beadOutline,
                                   final Paint  beadFill,
                                   final Stroke beadStroke)
    {
	super(beadDepth, beadDisplacement, beadOutline, beadFill, beadStroke);
        rect = new Rectangle2D.Double();
    }

    /**
     * <code>renderBead</code> renders features as simple rectangle.
     *
     * @param g2 a <code>Graphics2D</code>.
     * @param f a <code>Feature</code> to render.
     * @param context a <code>SequenceRenderContext</code> context.
     */
    protected void renderBead(final Graphics2D            g2,
                              final Feature               f,
                              final SequenceRenderContext context)
    {
        Location loc = f.getLocation();

        int min = loc.getMin();
        int max = loc.getMax();
        int dif = max - min;

	if (context.getDirection() == context.HORIZONTAL)
	{
	    double  posXW = context.sequenceToGraphics(min);
	    double  posYN = beadDisplacement;
	    double  width = Math.max(((double) (dif + 1)) * context.getScale(), 1.0);
	    double height = Math.min(beadDepth, width / 2.0);

	    // If the bead height occupies less than the full height
	    // of the renderer, move it down so that it is central
	    if (height < beadDepth)
		posYN += ((beadDepth - height) / 2.0);

	    rect.setRect(posXW, posYN,
                         Math.floor(width),
                         Math.floor(height));
	}
	else
	{
	    double  posXW = beadDisplacement;
	    double  posYN = context.sequenceToGraphics(min);
	    double height = Math.max(((double) dif + 1) * context.getScale(), 1.0);
	    double  width = Math.min(beadDepth, height / 2.0);

	    if (width < beadDepth)
		posXW += ((beadDepth - width) /  2.0);

	    rect.setRect(posXW, posYN,
                         Math.floor(width),
                         Math.floor(height));
	}

	g2.setPaint(beadFill);
	g2.fill(rect);

	g2.setStroke(beadStroke);
	g2.setPaint(beadOutline);
	g2.draw(rect);
    }

    /**
     * <code>getDepth</code> calculates the depth required by this
     * renderer to display its beads.
     *
     * @param context a <code>SequenceRenderContext</code>.
     *
     * @return a <code>double</code>.
     */
    public double getDepth(final SequenceRenderContext context)
    {
	// Get max depth of delegates using base class method
  	double maxDepth = super.getDepth(context);
  	return Math.max(maxDepth, (beadDepth + beadDisplacement));
    }
}
