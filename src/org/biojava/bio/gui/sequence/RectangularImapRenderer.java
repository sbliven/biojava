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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.net.URL;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.OptimizableFilter;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.net.URLFactory;

/**
 * <code>RectangularImapRenderer</code> is a decorator for
 * <code>RectangularBeadRenderer</code> which adds the ability to
 * create HTML image map coordinates which correspond to the feature
 * rendering produced by the <code>RectangularBeadRenderer</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public class RectangularImapRenderer
    implements BeadFeatureRenderer, ImageMapRenderer, Serializable
{
    private RectangularBeadRenderer renderer;
    private ImageMap imageMap;
    private URLFactory urlFactory;

    /**
     * Creates a new <code>RectangularImapRenderer</code>.
     *
     * @param renderer a <code>RectangularBeadRenderer</code>.
     * @param imageMap an <code>ImageMap</code>.
     * @param urlFactory a <code>URLFactory</code> which should be
     * capable of creating a suitable URL from each
     * <code>Feature</code> on the <code>Sequence</code> to be
     * rendered.
     */
    public RectangularImapRenderer(RectangularBeadRenderer renderer,
                                   ImageMap                imageMap,
                                   URLFactory              urlFactory)
    {
        this.renderer   = renderer;
        this.imageMap   = imageMap;
        this.urlFactory = urlFactory;
    }

    /**
     * <code>getImageMap</code> returns the current image map.
     *
     * @return an <code>ImageMap</code>.
     */
    public ImageMap getImageMap()
    {
        return imageMap;
    }

    /**
     * <code>setImageMap</code> sets the current image map.
     *
     * @param imageMap an <code>ImageMap</code>.
     */
    public void setImageMap(ImageMap imageMap)
    {
        this.imageMap = imageMap;
    }

    /**
     * <code>setDelegateRenderer</code> for the specified filter.
     *
     * @param filter an <code>OptimizableFilter</code>.
     * @param renderer a <code>BeadFeatureRenderer</code>.
     */
    public void setDelegateRenderer(final OptimizableFilter   filter,
                                    final BeadFeatureRenderer renderer)
    {
        this.renderer.setDelegateRenderer(filter, renderer);
    }

    /**
     * <p><code>renderImageMap</code> writes a set of image map
     * coordinates corresponding to the rectangle drawn by the
     * renderer. The hotspots created by this method have the rendered
     * <code>Feature</code> set as their user object.</p>
     *
     * <p>This method is called by <code>renderFeature</code> when a
     * raster image is rendered.</p>
     *
     * @param g2 a <code>Graphics2D</code>.
     * @param f a <code>Feature</code>.
     * @param context a <code>SequenceRenderContext</code>.
     */
    public void renderImageMap(final Graphics2D                 g2,
                               final Feature                     f,
                               final SequenceRenderContext context)
    {
        Rectangle bounds = g2.getDeviceConfiguration().getBounds();

        // Safe to cast as bounds come from raster
        int  mapWidth = (int) bounds.getWidth();
        int mapHeight = (int) bounds.getHeight();

        URL url = urlFactory.createURL(f);

        double        beadDepth = getBeadDepth();
        double beadDisplacement = getBeadDisplacement();

        AffineTransform t = g2.getTransform();
        double transX = t.getTranslateX();
        double transY = t.getTranslateY();

        int min, max, dif, x1, y1, x2, y2;
        double posXW, posYN, width, height;

        Location loc = f.getLocation();
        
        min = loc.getMin();
        max = loc.getMax();
        dif = max - min;

        if (context.getDirection() == context.HORIZONTAL)
        {
            posXW  = context.sequenceToGraphics(min);
            posYN  = beadDisplacement;
            width  = Math.max(((double) (dif + 1)) * context.getScale(), 1.0);
            height = Math.min(beadDepth, width / 2.0);

            // If the bead height occupies less than the full height
            // of the renderer, move it down so that it is central
            if (height < beadDepth)
                posYN += ((beadDepth - height) / 2.0);
        }
        else
        {
            posXW  = beadDisplacement;
            posYN  = context.sequenceToGraphics(min);
            height = Math.max(((double) dif + 1) * context.getScale(), 1.0);
            width  = Math.min(beadDepth, height / 2.0);

            if (width < beadDepth)
                posXW += ((beadDepth - width) /  2.0);
        }

        // Apply translation and round
        x1 = (int) Math.floor(posXW + transX);
        y1 = (int) Math.floor(posYN + transY);
        x2 = (int) Math.floor(posXW + width + transX);
        y2 = (int) Math.floor(posYN + height + transY);

        // If the whole rectangle is outside the image then ignore
        // it
        if (! (x1 > mapWidth || y1 > mapHeight || x2 < 0 || y2 < 0))
        {
            x1 = Math.max(x1, 0);
            y1 = Math.max(y1, 0);
            x2 = Math.min(x2, mapWidth);
            y2 = Math.min(y2, mapHeight);

            Integer [] coordinates = new Integer[4];
            coordinates[0] = new Integer(x1);
            coordinates[1] = new Integer(y1);
            coordinates[2] = new Integer(x2);
            coordinates[3] = new Integer(y2);

            imageMap.addHotSpot(new ImageMap.HotSpot(ImageMap.RECT,
                                                     url, coordinates, f));
        }
    }

    public void renderFeature(final Graphics2D                 g2,
                              final Feature                     f,
                              final SequenceRenderContext context)
    {
        renderImageMap(g2, f, context);
        renderer.renderFeature(g2, f, context);
    }

    public void renderBead(final Graphics2D                 g2,
                           final Feature                     f,
                           final SequenceRenderContext context)
    {
        renderer.renderBead(g2, f, context);
    }

    public double getDepth(final SequenceRenderContext context)
    {
        return renderer.getDepth(context);
    }

    public double getBeadDepth()
    {
        return renderer.getBeadDepth();
    }

    public double getBeadDisplacement()
    {
        return renderer.getBeadDisplacement();
    }

    public FeatureHolder processMouseEvent(final FeatureHolder         holder,
                                           final SequenceRenderContext context,
                                           final MouseEvent            mEvent)
    {
        return renderer.processMouseEvent(holder, context, mEvent);
    }
}
