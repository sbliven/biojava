//package seqviewer;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.List;  // Tie-breaker

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.gui.*;
import org.biojava.bio.gui.sequence.*;

public class EmblViewer {
  public static SequencePanel sp;
  static JFrame f;

  public static void main(String[] args) throws Exception {
    String seqFile = null;
    try {
	    seqFile = args[0];
    } catch (IndexOutOfBoundsException ex) {
	    throw new Exception("usage: java seqviewer.EmblViewer acc.embl");
    }

    final BasicFeatureRenderer fr;
    final FeatureRenderer split;
    
    SequenceFormat ef = new EmblFormat();
    SequenceFactory sf = new SimpleSequenceFactory();
    InputStream is = new FileInputStream(seqFile);
	    
    StreamReader sr = new StreamReader(is, ef, DNATools.getDNA().getParser("token"), sf);
    Sequence seq = sr.nextSequence();

    FeatureFilter notSource = new FeatureFilter.Not(
      new FeatureFilter.ByType("source")
    );
    FeatureFilter repeatFilter = new FeatureFilter.ByType("repeat_region");
    FeatureFilter miscFilter = new FeatureFilter.ByType("misc_feature");
    
    f = new JFrame("EMBL View");
    sp = new SequencePanel();
    sp.setSequence(seq);
    sp.setScale(20.0);
    sp.setSpacer(10);
    sp.setDirection(SequencePanel.HORIZONTAL);
    fr = new BasicFeatureRenderer();
    split = new CompoundFeatureRenderer();
    FeatureRenderer frChooser = new FeatureRenderer() {
      public void renderFeature(
        Graphics2D g, Feature f, Rectangle2D box, SequenceRenderContext context
      ) {
        if(f.getLocation().isContiguous()) {
          fr.renderFeature(g, f, box, context);
        } else {
          split.renderFeature(g, f, box, context);
        }
      }
    };
    FeatureBlockSequenceRenderer features = new FeatureBlockSequenceRenderer();
    FeatureBlockSequenceRenderer repeats = new FeatureBlockSequenceRenderer();
    FeatureBlockSequenceRenderer misc = new FeatureBlockSequenceRenderer();
    features.setDepth(15);
    repeats.setDepth(10);
    misc.setDepth(10);
    features.setFeatureRenderer(frChooser);
    repeats.setFeatureRenderer(fr);
    misc.setFeatureRenderer(frChooser);
    features.setLabel("features");
    repeats.setLabel("repeats");
    misc.setLabel("misc");
    features.setFilter(
      new FeatureFilter.And(
        new FeatureFilter.And(
          new FeatureFilter.Not(repeatFilter),
          new FeatureFilter.Not(miscFilter)
        ),
        notSource
      )
    );
    repeats.setFilter(repeatFilter);
    misc.setFilter(miscFilter);

    sp.addRenderer(repeats);
    sp.addRenderer(misc);
    sp.addRenderer(features);
    sp.addRenderer(new SymbolSequenceRenderer());
    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(new JScrollPane(sp), BorderLayout.CENTER);
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout());
    f.getContentPane().add(panel, BorderLayout.NORTH);
    
    JButton vert = new JButton("Vertical");
    vert.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
        sp.setDirection(SequencePanel.VERTICAL);
	    }
    } );
    JButton horiz = new JButton("Horizontal");
    horiz.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
        sp.setDirection(SequencePanel.HORIZONTAL);
	    }
    } );
    JButton blue = new JButton("Blue");
    blue.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
        fr.setFill(Color.blue);
	    }
    } );
    JButton red = new JButton("Red");
    red.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
        fr.setFill(Color.red);
	    }
    } );
    JScrollBar scale = new JScrollBar(JScrollBar.HORIZONTAL);
    scale.addAdjustmentListener(
      new AdjustmentListener() {
        public void adjustmentValueChanged(AdjustmentEvent e) {
          int val = e.getValue();
          double s = Math.exp(-val / 7.0) * 20.0;
          System.out.println("Scale now at " + s);
          sp.setScale(s);
        }
      } 
    );
    JScrollBar lines = new JScrollBar(JScrollBar.HORIZONTAL, 1, 1, 0, 5);
    lines.addAdjustmentListener(
      new AdjustmentListener() {
        public void adjustmentValueChanged(AdjustmentEvent e) {
          sp.setLines(e.getValue());
        }
      } 
    );
    
    panel.add(vert);
    panel.add(horiz);
    panel.add(blue);
    panel.add(red);
    panel.add(scale);
    panel.add(lines);

    f.setSize(500, 500);
    f.setVisible(true);
  }
  
  public static class CompoundFeatureRenderer implements FeatureRenderer {
    private Paint outline = Color.black;
    private Paint fill = Color.yellow;
    private double borderDepth = 3.0;
    public void renderFeature(
      Graphics2D g, Feature f, Rectangle2D box, SequenceRenderContext context
    ) {
      Location loc = f.getLocation();
      Iterator i = loc.blockIterator();
      Location last = null;
      if(i.hasNext()) {
        last = (Location) i.next();
        renderLocation(g, last, box, context);
      }
      while(i.hasNext()) {
        Location next = (Location) i.next();
        renderLink(g, f, last, next, box, context);
        renderLocation(g, next, box, context);
        last = next;
      }
    }
    
    private void renderLocation(
      Graphics2D g, Location loc, Rectangle2D box, SequenceRenderContext context
    ) {
      Rectangle2D.Double block = new Rectangle2D.Double();
      double min = sp.sequenceToGraphics(loc.getMin());
      double max = sp.sequenceToGraphics(loc.getMax()+1);
      if(sp.getDirection() == sp.HORIZONTAL) {
        block.setFrame(
          min, box.getMinY() + borderDepth,
          max - min, box.getHeight() - 2.0 * borderDepth
        );
      } else {
        block.setFrame(
          box.getMinX() + borderDepth, min,
          box.getHeight() - 2.0 * borderDepth, max - min
        );
      }
      g.setPaint(fill);
      g.fill(block);
      g.setPaint(outline);
      g.draw(block);
    }
    
    private void renderLink(
      Graphics2D g, Feature f, Location source, Location dest,
      Rectangle2D box, SequenceRenderContext context
    ) {
      Line2D line = new Line2D.Double();
      Point2D startP;
      Point2D midP;
      Point2D endP;
      if(sp.getDirection() == sp.HORIZONTAL) {
        if(
          (f instanceof StrandedFeature) &&
          (((StrandedFeature) f).getStrand() == StrandedFeature.NEGATIVE)
        ) {
          double start = sp.sequenceToGraphics(dest.getMin());
          double end = sp.sequenceToGraphics(source.getMax()+1);
          double mid = (start + end) * 0.5;
          startP = new Point2D.Double(start, box.getHeight() - borderDepth);
          midP   = new Point2D.Double(mid,   box.getHeight());
          endP   = new Point2D.Double(end,   box.getHeight() - borderDepth);
        } else {
          double start = sp.sequenceToGraphics(source.getMax());
          double end = sp.sequenceToGraphics(dest.getMin()+1);
          double mid = (start + end) * 0.5;
          startP = new Point2D.Double(start, borderDepth);
          midP   = new Point2D.Double(mid,   0);
          endP   = new Point2D.Double(end,   borderDepth);
        }
      } else {
        if(
          (f instanceof StrandedFeature) &&
          (((StrandedFeature) f).getStrand() == StrandedFeature.NEGATIVE)
        ) {
          double start = sp.sequenceToGraphics(dest.getMin());
          double end = sp.sequenceToGraphics(source.getMax()+1);
          double mid = (start + end) * 0.5;
          startP = new Point2D.Double(box.getHeight() - borderDepth, start);
          midP   = new Point2D.Double(box.getHeight(),               mid);
          endP   = new Point2D.Double(box.getHeight() - borderDepth, end);
        } else {
          double start = sp.sequenceToGraphics(source.getMax());
          double end = sp.sequenceToGraphics(dest.getMin()+1);
          double mid = (start + end) * 0.5;
          startP = new Point2D.Double(borderDepth, start);
          midP   = new Point2D.Double(0,           mid);
          endP   = new Point2D.Double(borderDepth, end);
        }
      }
      line.setLine(startP, midP);
      g.draw(line);
      line.setLine(midP, endP);
      g.draw(line);
    }
  }
}
