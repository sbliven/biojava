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

import java.util.*;
import java.beans.*;
import java.lang.reflect.*;
import java.lang.ref.*;

import java.awt.*;
import java.awt.geom.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;

import java.util.List;

public class AlignmentRenderer implements SequenceRenderer, PropertyChangeListener {
    private List renderList;
    protected PropertyChangeSupport pcs;
    protected Map cache;  // FIXME: is a simple cache the Right Way to solve
                          // performance questions?

    {
	renderList = new ArrayList();
	pcs = new PropertyChangeSupport(this);
	cache = new HashMap();
    }

    public void addRenderer(SequenceRenderer sr, Object label) {
	renderList.add(new LabelAndRenderer(sr, label));

	try {
	    BeanInfo bi = Introspector.getBeanInfo(sr.getClass());
	    EventSetDescriptor[] esd = bi.getEventSetDescriptors();
	    for (int i = 0; i < esd.length; ++i) {
		if (esd[i].getListenerType() == PropertyChangeListener.class) {
		    Method alm = esd[i].getAddListenerMethod();
		    Object[] args = { this };
		    alm.invoke(sr, args);
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public double getDepth(SequenceRenderContext ctx) {
	double depth = 0.0;
	for (Iterator i = renderList.iterator(); i.hasNext(); ) {
	    LabelAndRenderer lar = (LabelAndRenderer) i.next();
	    SequenceRenderContext subctx = new SequenceRenderContextForLabel(ctx, lar.getLabel());
	    depth += lar.getRenderer().getDepth(subctx);
	}
	return depth;
    }

    public double getMinimumLeader(SequenceRenderContext ctx) {
	double leader = 0.0;
	for (Iterator i = renderList.iterator(); i.hasNext(); ) {
	    LabelAndRenderer lar = (LabelAndRenderer) i.next();
	    SequenceRenderContext subctx = new SequenceRenderContextForLabel(ctx, lar.getLabel());
	    leader = Math.max(lar.getRenderer().getMinimumLeader(subctx), leader);
	}
	return leader;
    }

    public double getMinimumTrailer(SequenceRenderContext ctx) {
	double trailer = 0.0;
	for (Iterator i = renderList.iterator(); i.hasNext(); ) {
	    LabelAndRenderer lar = (LabelAndRenderer) i.next();
	    SequenceRenderContext subctx = new SequenceRenderContextForLabel(ctx, lar.getLabel());
	    trailer = Math.max(lar.getRenderer().getMinimumTrailer(subctx), trailer);
	}
	return trailer;
    }

    public void paint(Graphics2D g,
		      SequenceRenderContext ctx,
		      Rectangle2D seqBox)
    {
	double offset = 0.0;
	Rectangle2D subSeqBox = new Rectangle2D.Double();

	for (Iterator i = renderList.iterator(); i.hasNext(); ) {
	    LabelAndRenderer lar = (LabelAndRenderer) i.next();
	    SequenceRenderContext subctx = new SequenceRenderContextForLabel(ctx, lar.getLabel());

	    System.out.println(lar.getLabel().toString() + " " + offset);

	    double depth = lar.getRenderer().getDepth(subctx);
	    int dir = ctx.getDirection();
	    if (dir == ctx.HORIZONTAL) {
		g.translate(0.0, offset);
		subSeqBox.setRect(seqBox.getX(), seqBox.getY(),
				  seqBox.getWidth(), depth);
	    } else {
		g.translate(offset, 0.0);
		subSeqBox.setRect(seqBox.getX(), seqBox.getY(),
				  depth, seqBox.getHeight());
	    }

	    lar.getRenderer().paint(g, subctx, subSeqBox);

	    if (dir == ctx.HORIZONTAL) {
		g.translate(0.0, -offset);
	    } else {
		g.translate(-offset, 0.0);
	    }

	    offset += depth;
	}
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
      pcs.addPropertyChangeListener(l);
    }

    public void addPropertyChangeListener(String p, PropertyChangeListener l) {
      pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }

    public void removePropertyChangeListener(String p,
					     PropertyChangeListener l) {
	pcs.removePropertyChangeListener(p, l);
    }

    public void propertyChange(PropertyChangeEvent ev) {
	pcs.firePropertyChange("sequenceRenderer", null, null);
    }

    private class SequenceRenderContextForLabel implements SequenceRenderContext {
	private SequenceRenderContext parent;
	private Object label;

	private SequenceRenderContextForLabel(SequenceRenderContext parent,
					      Object label)
	{
	    this.parent = parent;
	    this.label = label;
	}

	public int getDirection() {
	    return parent.getDirection();
	}

	public double getScale() {
	    return parent.getScale();
	}

	public double sequenceToGraphics(int i) {
	    return parent.sequenceToGraphics(i);
	}

	public int graphicsToSequence(double d) {
	    return parent.graphicsToSequence(d);
	}

	public SymbolList getSequence() {
	    SymbolList pseq = parent.getSequence();
	    if (pseq instanceof Alignment) {
		Alignment aseq = (Alignment) pseq;
		LabelAlignment la = new LabelAlignment(aseq, label);
		Reference r = (Reference) cache.get(la);
		SymbolList sl = (r == null) ? null : (SymbolList) r.get();
		if (sl == null) {
		    sl = aseq.symbolListForLabel(label);
		    cache.put(la, new SoftReference(sl));
		}
		return sl;
	    } else {
		return pseq;
	    }
	}

	public SequenceRenderContext.Border getLeadingBorder() {
	    return parent.getLeadingBorder();
	}

	public SequenceRenderContext.Border getTrailingBorder() {
	    return parent.getTrailingBorder();
	}

	public Font getFont() {
	    return parent.getFont();
	}
    }

    private static class LabelAlignment {
	private Alignment alignment;
	private Object label;

	private LabelAlignment(Alignment a, Object l) {
	    alignment = a;
	    label = l;
	}

	public Alignment getAlignment() {
	    return alignment;
	}

	public Object getLabel() {
	    return label;
	}

	public boolean equals(Object o) {
	    if (! (o instanceof LabelAlignment))
		return false;
	    LabelAlignment la = (LabelAlignment) o;
	    return alignment.equals(la.getAlignment()) &&
                   label.equals(la.getLabel());
	}

	public int hashCode() {
	    return alignment.hashCode() + label.hashCode();
	}
    }

    private static class LabelAndRenderer {
	private Object label;
	private SequenceRenderer renderer;

	private LabelAndRenderer(SequenceRenderer sr, Object l) {
	    this.label = l;
	    this.renderer = sr;
	}

	public Object getLabel() {
	    return label;
	}

	public SequenceRenderer getRenderer() {
	    return renderer;
	}
    }
}
