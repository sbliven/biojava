package seqviewer;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
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
  static BasicFeatureRenderer fr;

  public static void main(String[] args) throws Exception {
    String seqFile = null;
    try {
	    seqFile = args[0];
    } catch (IndexOutOfBoundsException ex) {
	    throw new Exception("usage: java seqviewer.EmblViewer acc.embl");
    }

    SequenceFormat ef = new EmblFormat();
    SequenceFactory sf = new SimpleSequenceFactory();
    InputStream is = new FileInputStream(seqFile);
	    
    StreamReader sr = new StreamReader(is, ef, DNATools.getDNA().getParser("token"), sf);
    Sequence seq = sr.nextSequence();

    FeatureFilter notSource = new FeatureFilter.Not(
      new FeatureFilter.ByType("source")
    );
    FeatureFilter repeatFilter = new FeatureFilter.ByType("repeat_region");
    
    f = new JFrame("Sequence test");
    sp = new SequencePanel();
    sp.setSequence(seq);
    sp.setScale(20.0);
    sp.setDirection(SequencePanel.HORIZONTAL);
    fr = new BasicFeatureRenderer();
    FeatureBlockSequenceRenderer features = new FeatureBlockSequenceRenderer();
    FeatureBlockSequenceRenderer repeats = new FeatureBlockSequenceRenderer();
    features.setFeatureRenderer(fr);
    repeats.setFeatureRenderer(fr);
    features.setFilter(
      new FeatureFilter.And(
        new FeatureFilter.Not(repeatFilter),
        notSource
      )
    );
    repeats.setFilter(repeatFilter);

    sp.addRenderer(repeats);    
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
}
