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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

import org.biojava.bio.BioException;
import org.biojava.bio.gui.sequence.FeatureRenderer;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.OptimizableFilter;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

/**
 * <code>AbstractBeadFeatureRenderer</code> is a an abstract base
 * class for the creation of <code>FeatureRenderer</code>s which use a
 * 'string of beads' metaphor for displaying features. Each subclass
 * of <code>AbstractBeadFeatureRenderer</code> should override the
 * abstract method <code>renderBead()</code> and provide the drawing
 * routine for its particular bead type.
 *
 * <p>A concrete <code>BeadFeatureRenderer</code> may render a series
 * of features in more than one style by delegating to other
 * <code>BeadFeatureRenderer</code>s for the additional style(s). This
 * is achieved using the <code>setDelegateRenderer()</code> method
 * which associates an <code>OptimizableFilter</code> with a
 * <code>FeatureRenderer</code>. Any feature accepted by the filter is
 * rendered with the renderer while the remainder are rendered by the
 * current renderer.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public abstract class AbstractBeadFeatureRenderer extends AbstractChangeable
    implements FeatureRenderer
{

    protected Map delegates;

    /**
     * Creates a new <code>AbstractBeadFeatureRenderer</code> with no
     * delegates i.e. it will render all features itself, using its
     * own style settings.
     */
    public AbstractBeadFeatureRenderer()
    {
	delegates = new HashMap();
    }

    /**
     * <code>processMouseEvent</code> defines the behaviour on
     * revieving a mouse event.
     *
     * @param holder a <code>FeatureHolder</code> object.
     * @param context a <code>SequenceRenderContext</code> object.
     * @param mEvent a <code>MouseEvent</code> object.
     *
     * @return a <code>FeatureHolder</code> object.
     */
    public FeatureHolder processMouseEvent(final FeatureHolder         holder,
					   final SequenceRenderContext context,
					   final MouseEvent            mEvent)
    {
	return holder;
    }

    /**
     * <code>renderFeature</code> draws a feature using the supplied
     * graphics context. The rendering may be delegated to another
     * <code>FeatureRenderer</code> instance.
     *
     * @param g a <code>Graphics2D</code> context.
     * @param f a <code>Feature</code> to render.
     * @param context a <code>SequenceRenderContext</code> context.
     */
    public void renderFeature(Graphics2D g, Feature f, SequenceRenderContext context)
    {
	for (Iterator di = delegates.keySet().iterator(); di.hasNext();)
	{
	    FeatureFilter filter = (FeatureFilter) di.next();

	    if (filter.accept(f))
	    {
		System.err.println(filter + " accepted " + f);

		FeatureRenderer delegate =
		    (AbstractBeadFeatureRenderer) delegates.get(filter);

		delegate.renderFeature(g, f, context);
		return;
	    }
	}

	System.err.println("Rendering: " + f);

	renderBead(g, f, context);
    }

    /**
     * <code>renderBead</code> should be overridden by the concrete
     * <code>BeadFeatureRenderer</code>.
     *
     * @param g a <code>Graphics2D</code> contevt.
     * @param f a <code>Feature</code> to render.
     * @param context a <code>SequenceRenderContext</code> context.
     */
    protected abstract void renderBead(final Graphics2D            g,
				       final Feature               f,
				       final SequenceRenderContext context);

    /**
     * <code>setDelegateRenderer</code> associates an
     * <code>OptimizableFilter</code> with a
     * <code>FeatureRenderer</code>. Any feature accepted by the
     * filter will be passed to the associated renderer for
     * drawing. The <code>OptimizableFilter</code>s should be disjoint
     * with respect to each other (a feature may not be rendered more
     * than once).
     *
     * @param renderer a <code>FeatureRenderer</code> object.
     * @param filter an <code>OptimizableFilter</code> object.
     *
     * @exception IllegalArgumentException if the filter is not
     * disjoint with existing delegate filters.
     */
    public void setDelegateRenderer(final FeatureRenderer   renderer,
				    final OptimizableFilter filter)
	throws IllegalArgumentException
    {
	Set delegateFilters = delegates.keySet();

	if (delegateFilters.size() == 0)
	{
	    delegates.put(filter, renderer);
	}
	else
	{
	    for (Iterator fi = delegateFilters.iterator(); fi.hasNext();)
	    {
		OptimizableFilter thisFilter = (OptimizableFilter) fi.next();

		if (! thisFilter.isDisjoint(filter))
		{
		    throw new IllegalArgumentException("Unable to apply filter as it clashes with existing filter "
						       + thisFilter
						       + " (filters must be disjoint)");
		}
		else
		{
		    delegates.put(filter, renderer);
		    break;
		}
	    }
	}
    }

    /**
     * <code>getDepth</code> calculates the depth required by this
     * renderer to display its beads. It recurses through its delegate
     * renderers and returns the highest value. Concrete renderers
     * should override this method and supply code to calculate their
     * own depth. If a subclass needs to know the depth of its
     * delegates (as is likely if it has any) they can call this
     * method using <code>super.getDepth()</code>.
     *
     * @param context a <code>SequenceRenderContext</code> object.
     *
     * @return a <code>double</code> value.
     */
    public double getDepth(final SequenceRenderContext context)
    {
	Collection delegateRenderers = delegates.values();
	double maxDepth = 0.0d;

	if (delegateRenderers.size() == 0)
	{
	    return maxDepth;
	}
	else
	{
	    for (Iterator ri = delegateRenderers.iterator(); ri.hasNext();)
	    {
		maxDepth = Math.max(maxDepth, ((AbstractBeadFeatureRenderer) ri.next()).getDepth(context));
	    }

	    return maxDepth;
	}
    }
}
