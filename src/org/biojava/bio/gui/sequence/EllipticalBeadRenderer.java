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
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import javax.swing.JComponent;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

/**
 * <code>EllipticalBeadRenderer</code> renders features as
 * simple ellipses. Their outline and fill <code>Paint</code>,
 * <code>Stroke</code>, feature depth, Y-axis displacement are
 * configurable. Also configurable is the maximum ratio of long axis
 * to short axis of the ellipse - this prevents long features also
 * becoming ever wider and obscuring neighbours.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class EllipticalBeadRenderer extends AbstractBeadRenderer
    implements FeatureRenderer
{
    public static final ChangeType RATIO =
	new ChangeType("The shape of the features has changed",
		       "org.biojava.bio.gui.sequence.EllipticalBeadRenderer",
		       "RATIO", SequenceRenderContext.LAYOUT);

    protected double dimensionRatio;

    /**
     * Creates a new <code>EllipticalBeadRenderer</code> object
     * with the default settings.
     */
    public EllipticalBeadRenderer()
    {
	super();
	dimensionRatio = 2.0f;
    }

    /**
     * Creates a new <code>EllipticalBeadRenderer</code> object.
     *
     * @param beadDepth a <code>double</code> value.
     * @param beadDisplacement a <code>double</code> value.
     * @param beadOutline a <code>Paint</code> object.
     * @param beadFill a <code>Paint</code> object.
     * @param beadStroke a <code>Stroke</code> object.
     * @param dimensionRatio a <code>double</code> value.
     */
    public EllipticalBeadRenderer(double beadDepth,
				  double beadDisplacement,
				  Paint  beadOutline,
				  Paint  beadFill,
				  Stroke beadStroke,
				  double dimensionRatio)
    {
	super(beadDepth, beadDisplacement, beadOutline, beadFill, beadStroke);
	dimensionRatio = 2.0f;
    }

    /**
     * <code>renderBead</code> renders features as simple ellipse.
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

	if (context.getDirection() == context.HORIZONTAL)
	{
	    double  posXW = context.sequenceToGraphics(min);
	    double  posYN = beadDisplacement;
	    double  width = Math.max(((double) (dif + 1)) * context.getScale(), 1.0f);
	    double height = Math.min(beadDepth, width / dimensionRatio);

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
		posYN += ((beadDepth - height) / dimensionRatio);

	    shape = new Ellipse2D.Double(posXW, posYN, width, height);
	}
	else
	{
	    double  posXW = beadDisplacement;
	    double  posYN = context.sequenceToGraphics(min);
	    double height = Math.max(((double) dif + 1) * context.getScale(), 1.0f);
	    double  width = Math.min(beadDepth, height / dimensionRatio);

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
		posXW += ((beadDepth - width) /  dimensionRatio);

	    shape = new Ellipse2D.Double(posXW, posYN, width, height);
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

    /**
     * <code>getDimensionRatio</code> returns the maximum ratio of
     * long dimension to short dimension of the bead. This should be
     * equal, or greater than 1.
     *
     * @return a <code>double</code> value.
     */
    public double getDimensionRatio()
    {
	return dimensionRatio;
    }

    /**
     * <code>setDimensionRatio</code> sets the maximum ratio of
     * long dimension to short dimension of the bead. This should be
     * equal, or greater than 1.
     *
     * @param depth a <code>double</code> value.
     *
     * @exception ChangeVetoException if an error occurs.
     */
    public void setDimensionRatio(final double ratio) throws ChangeVetoException
    {
	if (ratio < 1.0f)
	    throw new ChangeVetoException("The long dimension may not be less than the short dimension (ratio >= 1.0)");

	if (hasListeners())
	{
	    ChangeSupport cs = getChangeSupport(SequenceRenderContext.LAYOUT);
	    synchronized(cs)
	    {
		ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.LAYOUT,
						 null, null,
						 new ChangeEvent(this, RATIO,
								 new Double(dimensionRatio),
								 new Double(ratio)));
		cs.firePreChangeEvent(ce);
		dimensionRatio= ratio;
		cs.firePostChangeEvent(ce);
	    }
	}
	else
	{
	    dimensionRatio = ratio;
	}
    }
}
