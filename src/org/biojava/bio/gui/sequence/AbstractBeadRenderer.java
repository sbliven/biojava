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
import java.util.WeakHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

import org.biojava.bio.Annotation;
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
import org.biojava.utils.ChangeAdapter;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;

/**
 * <code>AbstractBeadRenderer</code> is a an abstract base
 * class for the creation of <code>FeatureRenderer</code>s which use a
 * 'string of beads' metaphor for displaying features. Each subclass
 * of <code>AbstractBeadRenderer</code> should override the
 * abstract method <code>renderBead()</code> and provide the drawing
 * routine for its particular bead type.
 *
 * <p>A concrete <code>BeadRenderer</code> may render a series
 * of features in more than one style by delegating to other
 * <code>BeadRenderer</code>s for the additional style(s). This
 * is achieved using the <code>setDelegateRenderer()</code> method
 * which associates an <code>OptimizableFilter</code> with a
 * <code>FeatureRenderer</code>. Any feature accepted by the filter is
 * rendered with the renderer while the remainder are rendered by the
 * current renderer.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public abstract class AbstractBeadRenderer extends AbstractChangeable
    implements FeatureRenderer
{
    /**
     * constant <code>DISPLACEMENT</code> indicating a change to the
     * Y-axis displacement of the feature.
     */
    public static final ChangeType DISPLACEMENT =
	new ChangeType("The displacement of the features has changed",
		       "org.biojava.bio.gui.sequence.AbstractBeadRenderer",
		       "DISPLACEMENT", SequenceRenderContext.LAYOUT);
    
    /**
     * constant <code>DEPTH</code> indicating a change to the depth of
     * the renderer.
     */
    public static final ChangeType DEPTH =
	new ChangeType("The depth of the renderer has changed",
		       "org.biojava.bio.gui.sequence.AbstractBeadRenderer",
		       "DEPTH", SequenceRenderContext.LAYOUT);

    /**
     * constant <code>OUTLINE</code> indicating a change to the
     * outline paint of the feature.
     */
    public static final ChangeType OUTLINE =
	new ChangeType("The outline of the features has changed",
		       "org.biojava.bio.gui.sequence.AbstractBeadRenderer",
		       "OUTLINE", SequenceRenderContext.REPAINT);

    /**
     * constant <code>STROKE</code> indicating a change to the outline
     * stroke of the feature.
     */
    public static final ChangeType STROKE =
	new ChangeType("The stroke of the features has changed",
		       "org.biojava.bio.gui.sequence.AbstractBeadRenderer",
		       "STROKE", SequenceRenderContext.REPAINT);
    
    /**
     * constant <code>FILL</code> indicating a change to the fill of
     * the feature.
     */
    public static final ChangeType FILL =
	new ChangeType("The fill of the features has changed",
		       "org.biojava.bio.gui.sequence.AbstractBeadRenderer",
		       "FILL", SequenceRenderContext.REPAINT);

    protected double        beadDepth;
    protected double beadDisplacement;
    protected Paint       beadOutline;
    protected Paint          beadFill;
    protected Stroke       beadStroke;

    protected Map           delegates;
    protected Cache             cache;

    /**
     * Creates a new <code>AbstractBeadRenderer</code> with no
     * delegates i.e. it will render all features itself, using its
     * own style settings.
     */
    public AbstractBeadRenderer()
    {
	this(10.0f, 0.0f, Color.black, Color.black, new BasicStroke());
    }

    /**
     * Creates a new <code>AbstractBeadRenderer</code> object.
     *
     * @param beadDepth a <code>double</code> value.
     * @param beadDisplacement a <code>double</code> value.
     * @param beadOutline a <code>Paint</code> object.
     * @param beadFill a <code>Paint</code> object.
     * @param beadStroke a <code>Stroke</code> object.
     */
    AbstractBeadRenderer(double beadDepth,
			 double beadDisplacement,
			 Paint  beadOutline,
			 Paint  beadFill,
			 Stroke beadStroke)
    {
	this.beadDepth        = beadDepth;
	this.beadDisplacement = beadDisplacement;
	this.beadOutline      = beadOutline;
	this.beadFill         = beadFill;
	this.beadStroke       = beadStroke;

	delegates = new HashMap();
	cache     = new Cache();
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
	// Check the cache first
	if (cache.containsKey(f))
	{
	    // System.err.println("Used cache for: " + f);

	    AbstractBeadRenderer cachedRenderer =
		(AbstractBeadRenderer) cache.get(f);

	    cachedRenderer.renderBead(g, f, context);
	    return;
	}

	for (Iterator di = delegates.keySet().iterator(); di.hasNext();)
	{
	    FeatureFilter filter = (FeatureFilter) di.next();

	    if (filter.accept(f))
	    {
		// System.err.println(filter + " accepted " + f);

		FeatureRenderer delegate =
		    (AbstractBeadRenderer) delegates.get(filter);

		delegate.renderFeature(g, f, context);
		return;
	    }
	}

	cache.put(f, this);
	// System.err.println("Rendering: " + f);
	renderBead(g, f, context);
    }

    /**
     * <code>renderBead</code> should be overridden by the concrete
     * <code>BeadRenderer</code>.
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
     * @param filter an <code>OptimizableFilter</code> object.
     * @param renderer a <code>FeatureRenderer</code> object.
     *
     * @exception IllegalArgumentException if the filter is not
     * disjoint with existing delegate filters.
     */
    public void setDelegateRenderer(final OptimizableFilter filter,
				    final FeatureRenderer   renderer)
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
		maxDepth = Math.max(maxDepth, ((AbstractBeadRenderer) ri.next()).getDepth(context));
	    }

	    return maxDepth;
	}
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
     * <code>getBeadStroke</code> returns the bead outline stroke.
     *
     * @return a <code>Stroke</code> value.
     */
    public Stroke getBeadStroke()
    {
	return beadStroke;
    }

    /**
     * <code>setBeadStroke</code> sets the bead outline stroke.
     *
     * @param outline a <code>Stroke</code> value.
     *
     * @exception ChangeVetoException if an error occurs.
     */
    public void setBeadStroke(final Stroke outline) throws ChangeVetoException
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
								 beadStroke));
		cs.firePreChangeEvent(ce);
		beadStroke = outline;
		cs.firePostChangeEvent(ce);
	    }
	}
	else
	{
	    beadStroke = outline;
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

    /**
     * <p><code>Cache</code> to hold the direct mapping of
     * <code>Feature</code>s to their renderers. This is used to
     * bypass recursion through the delegate renderers once the
     * relationship has been established by an initial recursive
     * search.</p>
     *
     * <p>The <code>Feature</code>s register themselves with a
     * <code>ChangeListener</code> in the cache. When a feature
     * changes its rendered representation may need to be updated, so
     * the listener removes the feature from the cache. Currently
     * features are immutable, apart from their Annotation, so the
     * cache only listens for changes there. Features forward
     * ChangeEvents from their Annotation.</p>
     *
     * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
     * @since 1.2
     */
    private class Cache
    {
	private WeakHashMap map = new WeakHashMap();

	private ChangeListener listener = new ChangeAdapter()
	{
	    public void postChange(ChangeEvent cev)
	    {
		Changeable changedFeature = (Changeable) cev.getSource();
		map.remove(changedFeature);
		changedFeature.removeChangeListener(listener);
	    }
	};

	public void put(Object key, Object value)
	{
	    map.put(key, value);
	    if (Changeable.class.isInstance(key))
	    {
		((Changeable) key).addChangeListener(listener, Annotation.PROPERTY);
	    }
	}

	public Object get(Object key)
	{
	    return map.get(key);
	}

	public boolean containsKey(Object key)
	{
	    return map.containsKey(key);
	}
    }
}
