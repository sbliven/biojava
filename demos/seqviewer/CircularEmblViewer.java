package seqviewer;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FilterUtils;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.gui.sequence.*;
import org.biojava.bio.symbol.Location;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;
import java.util.Iterator;

/**
 * Demo for the circular viewer code that loads an embl file.
 *
 * <p>Use the file demos/files/AF438419.embl as an example.</p>
 * 
 * @author Matthew Pocock
 */
public class CircularEmblViewer {
  private static final double RADIUS = 200.0;
  public static void main(String[] args)
  throws Throwable {
    Sequence seq = SeqIOTools.readEmbl(
            new BufferedReader(
                    new FileReader(
                            new File(args[0])))
    ).nextSequence();

    JFrame frame = new JFrame("Circular Viewer");
    frame.getContentPane().setLayout(new BorderLayout());

    CircularMLR mlr = new CircularMLR();

    CircularFeatureRenderer cfr = new TorusRenderer(Color.BLACK, Color.BLUE, 8.0);
    CircularFeaturesRenderer cfrs = new CircularFeaturesRenderer(cfr);
    mlr.addRenderer(new CircularPaddedRenderer(
            new CircularFeatureFilteringRenderer(
                    cfrs,
                    FilterUtils.byType("gene"),
                    false),
            2.0, 2.0));
    mlr.addRenderer(new CircularPaddedRenderer(
            new CircularFeatureFilteringRenderer(
                    cfrs,
                    FilterUtils.byType("CDS"),
                    false),
            2.0, 2.0));
    mlr.addRenderer(new CircularPaddedRenderer(
            new CircularFeatureFilteringRenderer(
                    cfrs,
                    FilterUtils.byType("repeat_region"),
                    false),
            2.0, 2.0));

    CircularRendererPanel crPanel = new CircularRendererPanel();
    crPanel.setRadius(RADIUS);
    crPanel.setSequence(seq);
    crPanel.setRenderer(mlr);

    frame.getContentPane().add(new JScrollPane(crPanel), BorderLayout.CENTER);
    frame.setSize((int) ((RADIUS + 100.0) * 2.0),
                  (int) ((RADIUS + 100.0) * 2.0));
    frame.setVisible(true);
  }

  private static final class TorusRenderer
  implements CircularFeatureRenderer {
    private double depth;
    private Paint fill;
    private Paint outline;

    public TorusRenderer(Paint outline, Paint fill, double depth) {
      this.outline = outline;
      this.fill = fill;
      this.depth = depth;
    }

    public double getDepth(CircularRendererContext crc) {
      return depth + 1.0;
    }

    public void renderFeature(
            Graphics2D g2,
            Feature f,
            CircularRendererContext context)
    {
      Rectangle2D outer = GUITools.createOuterBounds(context, getDepth(context));
      Rectangle2D inner = GUITools.createInnerBounds(context);
      Rectangle2D mid = new Rectangle2D.Double(
              (outer.getMinX()    + inner.getMinX())    * 0.5,
              (outer.getMinY()    + inner.getMinY())    * 0.5,
              (outer.getWidth()   + inner.getWidth())   * 0.5,
              (outer.getHeight()  + inner.getHeight())  * 0.5 );


      Location loc = f.getLocation();

      double startA = Math.toDegrees(context.getAngle(loc.getMin()));
      double endA = Math.toDegrees(context.getAngle(loc.getMax()));
      Arc2D midArc = new Arc2D.Double(mid, startA, endA - startA, Arc2D.OPEN);

      g2.setPaint(outline);
      g2.draw(midArc);

      for(Iterator i = loc.blockIterator(); i.hasNext(); ) {
        Location l = (Location) i.next();
        startA = Math.toDegrees(context.getAngle(l.getMin()));
        endA = Math.toDegrees(context.getAngle(l.getMax()));

        Arc2D outerArc = new Arc2D.Double(
                outer, startA, endA - startA, Arc2D.OPEN);
        Arc2D innerArc = new Arc2D.Double(
                inner, endA, startA - endA, Arc2D.OPEN);

        GeneralPath path = new GeneralPath();
        path.append(outerArc, true);
        path.append(innerArc, true);
        path.closePath();
        g2.setPaint(fill);
        g2.fill(path);
        g2.setPaint(outline);
        g2.draw(path);
      }
    }
  }
}
