package seqviewer;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.gui.sequence.CircularRendererPanel;
import org.biojava.bio.gui.sequence.CircularRenderer;
import org.biojava.bio.gui.sequence.CircularRendererContext;
import org.biojava.bio.gui.sequence.GUITools;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;

/**
 * Demo for the circular viewer code that loads an embl file.
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

    CircularRenderer mlr = new CircleRenderer();

    CircularRendererPanel crPanel = new CircularRendererPanel();
    crPanel.setRadius(RADIUS);
    crPanel.setSequence(seq);
    crPanel.setRenderer(mlr);

    frame.getContentPane().add(new JScrollPane(crPanel), BorderLayout.CENTER);
    frame.setSize((int) ((RADIUS + 100.0) * 2.0),
                  (int) ((RADIUS + 100.0) * 2.0));
    frame.setVisible(true);
  }

  private static final class CircleRenderer
  implements CircularRenderer {
    public double getDepth(CircularRendererContext crc) {
      return 3.0;
    }

    public void paint(Graphics2D g2, CircularRendererContext crc) {
      Rectangle2D rect = GUITools.createBounds(crc, this);
      Arc2D arc = new Arc2D.Double(
              GUITools.createBounds(crc, this),
              0,
              360.0 * 0.3,
              Arc2D.OPEN );
      g2.draw(arc);

      Ellipse2D elipse = new Ellipse2D.Double(
              rect.getX() + 2.0, rect.getY() + 2.0,
              rect.getWidth() - 4.0, rect.getHeight() - 4.0);
      g2.draw(elipse);
    }
  }
}
