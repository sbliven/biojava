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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

/**
 * <code>SimpleBeadFeatureRenderer</code> is a very basic example
 * <code>BeadFeatureRenderer</code> which renders features as simple
 * coloured rectangles. By delegating to other instances of this class
 * which have different fill and outline colours, bead depths and bead
 * displacements this renderer will draw features in a range of styles
 * according to their characteristics. Alternatively, by delegating to
 * completely different styles of renderer, a mixture of different
 * shapes, colours, transparencies, reliefs etc. may be achieved on a
 * single line.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class SimpleBeadFeatureRenderer extends AbstractBeadFeatureRenderer
    implements FeatureRenderer
{
    public static final ChangeType DISPLACEMENT =
	new ChangeType("The displacement of the features has changed",
		       "org.biojava.bio.gui.sequence.SimpleBeadFeatureRenderer",
		       "DISPLACEMENT", SequenceRenderContext.LAYOUT);
    
    public static final ChangeType DEPTH =
	new ChangeType("The depth of the renderer has changed",
		       "org.biojava.bio.gui.sequence.SimpleBeadFeatureRenderer",
		       "DEPTH", SequenceRenderContext.LAYOUT);

    public static final ChangeType OUTLINE =
	new ChangeType("The outline of the features has changed",
		       "org.biojava.bio.gui.sequence.SimpleBeadFeatureRenderer",
		       "OUTLINE", SequenceRenderContext.REPAINT);
    
    public static final ChangeType FILL =
	new ChangeType("The outline of the features has changed",
		       "org.biojava.bio.gui.sequence.SimpleBeadFeatureRenderer",
		       "FILL", SequenceRenderContext.REPAINT);

    private double        beadDepth = 10.0f;
    private double beadDisplacement = 0.0f;
    private Paint       beadOutline = Color.blue;
    private Paint          beadFill = Color.blue;

    /**
     * <code>renderBead</code> renders features as simple rectangular
     * blocks.
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

	if (context.getDirection() == context.HORIZONTAL)
	{
	    float minX = min;
	    float maxX = max;
	    float minY = (float) beadDisplacement;
	    float maxY = (float) (beadDisplacement + beadDepth) - 1.0f;

	    GeneralPath path = new GeneralPath();
	    path.moveTo(minX, minY);
	    path.lineTo(minX, maxY);
	    path.lineTo(maxX, maxY);
	    path.lineTo(maxX, minY);
	    path.closePath();

	    shape = path;
	}
	else
	{
	    float minX = (float) beadDisplacement;
	    float maxX = (float) (beadDisplacement + beadDepth) - 1.0f;
	    float minY = min;
	    float maxY = max;

	    GeneralPath path = new GeneralPath();
	    path.moveTo(minX, minY);
	    path.lineTo(minX, maxY);
	    path.lineTo(maxX, maxY);
	    path.lineTo(maxX, minY);
	    path.closePath();

	    shape = path;
	}

	g.setPaint(beadFill);
	g.fill(shape);

	g.setPaint(beadOutline);
	g.draw(shape);
    }

    /**
     * <code>getDepth</code> calculates the depth required to
     * represent the features belonging to this renderer and its
     * delegate renderers.
     *
     * @param context a <code>SequenceRenderContext</code> context.
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
     * <code>getBeadDepth</code> returns the depth of a single bead
     * produced by this renderer.
     *
     * @return a <code>double</code> value.
     */
    public double getBeadDepth()
    {
	return beadDepth;
    }

    /**
     * <code>setBeadDepth</code> sets the depth of a single bead
     * produced by this renderer.
     *
     * @param depth a <code>double</code> value.
     *
     * @exception ChangeVetoException if an error occurs.
     */
    public void setBeadDepth(final double depth) throws ChangeVetoException
    {
	if (hasListeners())
	{
	    ChangeSupport cs = getChangeSupport(SequenceRenderContext.LAYOUT);
	    synchronized(cs)
	    {
		ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.LAYOUT,
						 null, null,
						 new ChangeEvent(this, DEPTH,
								 new Double(beadDepth),
								 new Double(depth)));
		cs.firePreChangeEvent(ce);
		beadDepth = depth;
		cs.firePostChangeEvent(ce);
	    }
	}
	else
	{
	    beadDepth = depth;
	}
    }

    /**
     * <code>getBeadDisplacement</code> returns the displacement of
     * beads from the centre line of the renderer. A positive value
     * indicates displacment downwards (for horizontal renderers) or
     * to the right (for vertical renderers).
     *
     * @return a <code>double</code> value.
     */
    public double getBeadDisplacement()
    {
	return beadDisplacement;
    }

    /**
     * <code>setBeadDisplacement</code> sets the displacement of
     * beads from the centre line of the renderer. A positive value
     * indicates displacment downwards (for horizontal renderers) or
     * to the right (for vertical renderers).
     *
     * @param displacement a <code>double</code> value.
     *
     * @exception ChangeVetoException if an error occurs.
     */
    public void setBeadDisplacement(final double displacement) throws ChangeVetoException
    {
	if (hasListeners())
	{
	    ChangeSupport cs = getChangeSupport(SequenceRenderContext.LAYOUT);
	    synchronized(cs)
	    {
		ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.LAYOUT,
						 null, null,
						 new ChangeEvent(this, DISPLACEMENT,
								 new Double(beadDisplacement),
								 new Double(displacement)));
		cs.firePreChangeEvent(ce);
		beadDisplacement = displacement;
		
		cs.firePostChangeEvent(ce);
	    }
	}
	else
	{
	    beadDisplacement = displacement;
	}
    }

    /**
     * <code>getBeadOutline</code> returns the bead outline paint.
     *
     * @return a <code>Paint</code> value.
     */
    public Paint getBeadOutline()
    {
	return beadOutline;
    }

    /**
     * <code>setBeadOutline</code> sets the bead outline paint.
     *
     * @param outline a <code>Paint</code> value.
     *
     * @exception ChangeVetoException if an error occurs.
     */
    public void setBeadOutline(final Paint outline) throws ChangeVetoException
    {
	if (hasListeners())
	{
	    ChangeSupport cs = getChangeSupport(SequenceRenderContext.LAYOUT);
	    synchronized(cs)
	    {
		ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.LAYOUT,
						 null, null,
						 new ChangeEvent(this, OUTLINE,
								 outline,
								 beadOutline));
		cs.firePreChangeEvent(ce);
		beadOutline = outline;
		cs.firePostChangeEvent(ce);
	    }
	}
	else
	{
	    beadOutline = outline;
	}
    }

    /**
     * <code>getBeadFill</code> returns the bead fill paint.
     *
     * @return a <code>Paint</code> value.
     */
    public Paint getBeadFill()
    {
	return beadFill;
    }

    /**
     * <code>setBeadFill</code> sets the bead fill paint.
     *
     * @param fill a <code>Paint</code> value.
     *
     * @exception ChangeVetoException if an error occurs.
     */
    public void setBeadFill(final Paint fill) throws ChangeVetoException
    {
	if (hasListeners())
	{
	    ChangeSupport cs = getChangeSupport(SequenceRenderContext.LAYOUT);
	    synchronized(cs)
	    {
		ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.LAYOUT,
						 null, null,
						 new ChangeEvent(this, FILL,
								 fill,
								 beadFill));
		cs.firePreChangeEvent(ce);
		beadFill = fill;
		cs.firePostChangeEvent(ce);
	    }
	}
	else
	{
	    beadFill = fill;
	}
    }
}
