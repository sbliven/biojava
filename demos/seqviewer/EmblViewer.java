package seqviewer;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.List;  // Tie-breaker

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.impl.*;
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
    
    SequenceFormat ef = new EmblLikeFormat();
    SequenceBuilderFactory sf = new EmblProcessor.Factory(SimpleSequenceBuilder.FACTORY);
    InputStream is = new FileInputStream(seqFile);
	    
    StreamReader sr = new StreamReader(is, ef, DNATools.getDNA().getParser("token"), sf);
    Sequence seq = sr.nextSequence();

    Feature source = (Feature) seq.filter(
      new FeatureFilter.ByType("source"), false
    ).features().next();
    seq.removeFeature(source);
    
    FeatureFilter repeatFilter = new FeatureFilter.ByType("repeat_region");
    FeatureFilter miscFilter = new FeatureFilter.ByType("misc_feature");
    
    f = new JFrame("EMBL View");
    sp = new SequencePanel();
    sp.setSequence(seq);
    sp.setScale(20.0);
    sp.setSpacer(10);
    sp.setDirection(SequencePanel.HORIZONTAL);
    
    fr = new BasicFeatureRenderer();
    split = new ZiggyFeatureRenderer();
    FeatureRenderer frChooser = new FeatureRenderer() {
      public void renderFeature(
        Graphics2D g, Feature f, SequenceRenderContext context
      ) {
        if(f.getLocation().isContiguous()) {
          fr.renderFeature(g, f, context);
        } else {
          split.renderFeature(g, f, context);
        }
      }
      
      public double getDepth(SequenceRenderContext context) {
        return Math.max(fr.getDepth(context), split.getDepth(context));
      }
    };
    FeatureBlockSequenceRenderer features = new FeatureBlockSequenceRenderer();
    FeatureBlockSequenceRenderer repeats = new FeatureBlockSequenceRenderer();
    FeatureBlockSequenceRenderer misc = new FeatureBlockSequenceRenderer();
    
//    features.setDepth(15);
//    repeats.setDepth(10);
//    misc.setDepth(10);
    
    features.setFeatureRenderer(frChooser);
    repeats.setFeatureRenderer(fr);
    misc.setFeatureRenderer(frChooser);
    
//    features.setLabel("features");
//    repeats.setLabel("repeats");
//    misc.setLabel("misc");
    
    FeatureFilter featuresFilter = new FeatureFilter.And(
      new FeatureFilter.Not(repeatFilter),
      new FeatureFilter.Not(miscFilter)
    );

    BumpedRenderer lsr = new BumpedRenderer();
    lsr.setRenderer(features);
    
    MultiLineRenderer mlRend = new MultiLineRenderer();
    mlRend.addRenderer(new FilteringRenderer(repeats, repeatFilter, false));
    mlRend.addRenderer(new FilteringRenderer(misc, miscFilter, false));
    mlRend.addRenderer(new FilteringRenderer(lsr, featuresFilter, false));
    mlRend.addRenderer(new SymbolSequenceRenderer());
    mlRend.addRenderer(new RulerRenderer());

    sp.addRenderer(mlRend);
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
        try {
          fr.setFill(Color.blue);
        } catch (ChangeVetoException cve) {
          throw new BioError(cve, "oops");
        }
      }
    } );
    JButton red = new JButton("Red");
    red.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          fr.setFill(Color.red);
        } catch (ChangeVetoException cve) {
          throw new BioError(cve, "oops");
        }
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
}
