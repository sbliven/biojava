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
import java.awt.geom.RoundRectangle2D;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.Location;

/**
 * <code>RoundRectangularBeadRenderer</code> renders features
 * as rectangles with rounded corners. Their outline and fill
 * <code>Paint</code>, <code>Stroke</code>, feature depth, Y-axis
 * displacement are configurable.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class RoundRectangularBeadRenderer extends AbstractBeadRenderer
    implements FeatureRenderer
{
    protected double arcWidth;
    protected double arcHeight;

    /**
     * Creates a new <code>RoundRectangularBeadRenderer</code>
     * object with the default settings.
     */
    public RoundRectangularBeadRenderer()
    {
	super();
	arcWidth  = 5.0f;
	arcHeight = 5.0f;
    }

    /**
     * Creates a new <code>RoundRectangularBeadRenderer</code>
     * object.
     *
     * @param beadDepth a <code>double</code> value.
     * @param beadDisplacement a <code>double</code> value.
     * @param beadOutline a <code>Paint</code> object.
     * @param beadFill a <code>Paint</code> object.
     * @param beadStroke a <code>Stroke</code> object.
     * @param arcWidth a <code>double</code> value which sets the arc
     * width of the corners.
     * @param arcHeight a <code>double</code> value which sets the arc
     * height of the corners.
     */
    public RoundRectangularBeadRenderer(double beadDepth,
					double beadDisplacement,
					Paint  beadOutline,
					Paint  beadFill,
					Stroke beadStroke,
					double arcWidth,
					double arcHeight)
    {
	super(beadDepth, beadDisplacement, beadOutline, beadFill, beadStroke);
	arcWidth  = 5.0f;
	arcHeight = 5.0f;
    }

    /**
     * <code>renderBead</code> renders features as a rectangle with
     * rounded corners.
     *
     * @param g a <code>Graphics2D</code> context.
     * @param f a <code>Feature</code> to render.
     * @param context a <code>SequenceRenderContext</code> context.
     */
    protected void renderBead(final Graphics2D            g,
			      final Feature               f,
			      final SequenceRenderContext context)
    {
	Location loc = f.getLocation();

	float min = (float) context.sequenceToGraphics(loc.getMin());
	float max = (float) context.sequenceToGraphics(loc.getMax());

	Shape shape;

	float  arcWidth = 10.0f;
	float arcHeight = 10.0f;

	if (context.getDirection() == context.HORIZONTAL)
	{
	    float  posXW = min;
	    float  posYN = (float) beadDisplacement;
	    float  width = max - posXW + 1.0f;
	    float height = Math.min((float) beadDepth, width / 2.0f) - 1.0f;

	    // If the bead height occupies less than the full height
	    // of the renderer, move it down so that it is central
	    if (height < beadDepth)
		posYN += ((beadDepth - height) / 2.0f);

	    shape = new RoundRectangle2D.Float(posXW, posYN,
					       width, height,
					       arcWidth, arcHeight);
	}
	else
	{
	    float  posXW = (float) beadDisplacement;
	    float  posYN = min;
	    float height = max - posYN + 1.0f;
	    float  width = Math.min((float) beadDepth, height / 2.0f) - 1.0f;

	    if (width < beadDepth)
		posXW += ((beadDepth - height) /  2.0f);

	    shape = new RoundRectangle2D.Float(posXW, posYN,
					       width, height,
					       arcWidth, arcHeight);
	}

	g.setPaint(beadFill);
	g.fill(shape);

	g.setStroke(beadStroke);
	g.setPaint(beadOutline);
	g.draw(shape);
    }

    /**
     * <code>getDepth</code> calculates the depth required by this
     * renderer to display its beads.
     *
     * @param context a <code>SequenceRenderContext</code> object.
     *
     * @return a <code>double</code> value.
     */
    public double getDepth(final SequenceRenderContext context)
    {
	// Get max depth of delegates using base class method
  	double maxDepth = super.getDepth(context);
  	return Math.max(maxDepth, (beadDepth + beadDisplacement));
    }
}
