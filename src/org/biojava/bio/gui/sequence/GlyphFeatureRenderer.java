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

import org.biojava.bio.gui.glyph.Glyph;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

import java.util.*;
import java.util.List;


/**
 * A FeatureRenderer that renders a particular Glyph for Features accepted by a particular
 * FeatureFilter
 *
 * @author Mark Southern
 * @see org.biojava.bio.gui.glyph.Glyph
 * @since 1.5
 */
public class GlyphFeatureRenderer extends FilteringRenderer implements FeatureRenderer {
    private double depth = 15;
    private List fList = new ArrayList();
    private List gList = new ArrayList();

    public GlyphFeatureRenderer() {
        super();
    }

    public void addFilterAndGlyph(FeatureFilter ff, Glyph g)
        throws ChangeVetoException {
        fList.add(ff);
        gList.add(g);

        if (fList.size() == 0) {
            setFilter(FeatureFilter.none);
        } else {
            FeatureFilter f = ( FeatureFilter ) fList.get(0);

            if (fList.size() == 1) {
                setFilter(f);
            } else {
                for (int i = 1; i < fList.size(); i++) {
                    f = new FeatureFilter.Or(f, ( FeatureFilter ) fList.get(i));
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

    public FeatureHolder processMouseEvent(FeatureHolder fh, SequenceRenderContext src,
        MouseEvent me
    ) {
        return fh;
    }

    public void renderFeature(Graphics2D g2, Feature f, SequenceRenderContext src) {
        float minBounds = ( float ) src.sequenceToGraphics(f.getLocation().getMin());
        float maxBounds = ( float ) src.sequenceToGraphics(f.getLocation().getMax() + 1);
        Rectangle2D.Float bounds;
        bounds = new Rectangle2D.Float(minBounds, 0, maxBounds - minBounds, ( float ) depth);

        for (int i = 0; i < fList.size(); i++) {
            if ((( FeatureFilter ) fList.get(i)).accept(f)) {
                Glyph g = ( Glyph ) gList.get(i);
                g.setBounds(bounds);

                if (src.getDirection() == SequenceRenderContext.HORIZONTAL) {
                    g.render(g2);
                }
            }
        }
    }
}
