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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

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

	int min = loc.getMin();
	int max = loc.getMax();
	int dif = max - min;

	Shape shape;

	double  arcWidth = 10.0f;
	double arcHeight = 10.0f;

	if (context.getDirection() == context.HORIZONTAL)
	{
	    double  posXW = min;
	    double  posYN = beadDisplacement;
	    double  width = Math.max(((double) (dif + 1)) * context.getScale(), 1.0f);
	    double height = Math.min(beadDepth, width / 2.0f);

	    // This is an optimization for cases where the
	    // SequenceRenderContext also extend a JComponent and can
	    // inform us of their visible area
	    if (JComponent.class.isInstance(context))
	    {
		Rectangle visible = ((JComponent) context).getVisibleRect();
		
		// Hack! Compensates for offset in SequencePanel

		// Remove the contribution of any scroll offset from the
		// transform
		AffineTransform t = g.getTransform();
		t.translate(visible.getX(), visible.getY());

		// Invert the remainder and apply to cancel out the
		// remaining offset to visible
		try
		{
		    if (! t.createInverse().createTransformedShape(visible)
			.intersects(posXW, posYN, width, height))
			return;
  		}
    		catch (NoninvertibleTransformException ni)
    		{
    		    ni.printStackTrace();
    		}
  	    }

	    // If the bead height occupies less than the full height
	    // of the renderer, move it down so that it is central
	    if (height < beadDepth)
		posYN += ((beadDepth - height) / 2.0f);

	    shape = new RoundRectangle2D.Double(posXW, posYN,
						width, height,
						arcWidth, arcHeight);
	}
	else
	{
	    double  posXW = beadDisplacement;
	    double  posYN = min;
	    double height = Math.max(((double) dif + 1) * context.getScale(), 1.0f);
	    double  width = Math.min(beadDepth, height / 2.0f);

	    if (JComponent.class.isInstance(context))
	    {
		Rectangle visible = ((JComponent) context).getVisibleRect();

		AffineTransform t = g.getTransform();
		t.translate(visible.getX(), visible.getY());

		try
		{
		    if (! t.createInverse().createTransformedShape(visible)
			.intersects(posXW, posYN, width, height))
			return;
  		}
    		catch (NoninvertibleTransformException ni)
    		{
    		    ni.printStackTrace();
    		}
  	    }

	    if (width < beadDepth)
		posXW += ((beadDepth - width) /  2.0f);

	    shape = new RoundRectangle2D.Double(posXW, posYN,
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
