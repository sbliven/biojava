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
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.gui.glyph.ArrowGlyph;
import org.biojava.bio.gui.glyph.Glyph;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.utils.ChangeVetoException;

/**
 * A FeatureRenderer that renders a particular Glyph for Features accepted by a
 * particular FeatureFilter
 *
 * @author Mark Southern
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * @see org.biojava.bio.gui.glyph.Glyph
 * @since 1.5
 */
public class GlyphFeatureRenderer extends FilteringRenderer implements
    FeatureRenderer {
	private double	            depth	= 15;

	private List<FeatureFilter>	fList;

	private List<Glyph>	        gList;

	public GlyphFeatureRenderer() {
		super();
		fList = new ArrayList<FeatureFilter>();
		gList = new ArrayList<Glyph>();
	}

	public void addFilterAndGlyph(FeatureFilter ff, Glyph g)
	    throws ChangeVetoException {
		fList.add(ff);
		gList.add(g);

		if (fList.size() == 0) {
			setFilter(FeatureFilter.none);
		} else {
			FeatureFilter f = fList.get(0);

			if (fList.size() == 1) {
				setFilter(f);
			} else {
				for (int i = 1; i < fList.size(); i++) {
					f = new FeatureFilter.Or(f, fList.get(i));
				}

				setFilter(f);
			}
		}
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}

	public double getDepth(SequenceRenderContext src) {
		return depth;
	}

	public FeatureHolder processMouseEvent(FeatureHolder fh,
	    SequenceRenderContext src, MouseEvent me) {
		return fh;
	}

	public void renderFeature(Graphics2D g2, Feature f, SequenceRenderContext src) {
		float minBounds = (float) src.sequenceToGraphics(f.getLocation().getMin());
		float maxBounds = (float) src
		    .sequenceToGraphics(f.getLocation().getMax() + 1);
		Rectangle2D.Float bounds;
		bounds = new Rectangle2D.Float(minBounds, 0, maxBounds - minBounds,
		    (float) depth);

		for (int i = 0; i < fList.size(); i++)
			if (fList.get(i).accept(f)) {
				Glyph g = gList.get(i);
				g.setBounds(bounds);
				if ((g instanceof ArrowGlyph) && (f instanceof StrandedFeature))
					((ArrowGlyph) g).setDirection(((StrandedFeature) f).getStrand().getValue());
				if (src.getDirection() == SequenceRenderContext.HORIZONTAL)
				  g.render(g2);
			}
	}
}
