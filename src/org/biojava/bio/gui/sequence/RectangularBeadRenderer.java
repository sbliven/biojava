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
import javax.swing.JComponent;

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
    /**
     * Creates a new <code>RectangulerBeadRenderer</code> object.
     *
     * @param beadDepth a <code>double</code> value.
     * @param beadDisplacement a <code>double</code> value.
     * @param beadOutline a <code>Paint</code> object.
     * @param beadFill a <code>Paint</code> object.
     * @param beadStroke a <code>Stroke</code> object.
     */
    public RectangularBeadRenderer(double beadDepth,
				   double beadDisplacement,
				   Paint  beadOutline,
				   Paint  beadFill,
				   Stroke beadStroke)
    {
	super(beadDepth, beadDisplacement, beadOutline, beadFill, beadStroke);
    }

    /**
     * <code>renderBead</code> renders features as simple rectangle.
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

	double min = context.sequenceToGraphics(loc.getMin());
	double max = context.sequenceToGraphics(loc.getMax());

	Shape shape;

	if (context.getDirection() == context.HORIZONTAL)
	{
	    double  posXW = min;
	    double  posYN = beadDisplacement;
	    double  width = max - posXW + 1.0f;
	    double height = Math.min(beadDepth, width / 2.0f) - 1.0f;

	    if (JComponent.class.isInstance(context))
		if (! ((JComponent) context).getVisibleRect().intersects(posXW, posYN, width, height))
		    return;

	    // If the bead height occupies less than the full height
	    // of the renderer, move it down so that it is central
	    if (height < beadDepth)
		posYN += ((beadDepth - height) / 2.0f);

	    shape = new Rectangle2D.Double(posXW, posYN, width, height);
	}
	else
	{
	    double  posXW = beadDisplacement;
	    double  posYN = min;
	    double height = max - posYN + 1.0f;
	    double  width = Math.min(beadDepth, height / 2.0f) - 1.0f;

	    if (JComponent.class.isInstance(context))
		if (! ((JComponent) context).getVisibleRect().intersects(posXW, posYN, width, height))
		    return;

	    if (width < beadDepth)
		posXW += ((beadDepth - width) /  2.0f);

	    shape = new Rectangle2D.Double(posXW, posYN, width, height);
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
