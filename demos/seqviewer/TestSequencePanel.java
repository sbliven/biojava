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

public class TestSequencePanel {
    public static SequencePanel sp;
    static JFrame f;
    static BasicFeatureRenderer fr;
    static FeatureBlockSequenceRenderer features;

    public static void main(String[] args) throws Exception {
	String seqFile = null;
	try {
	    seqFile = args[0];
	} catch (IndexOutOfBoundsException ex) {
	    throw new Exception("usage: java seqviewer.TestSequencePanel seqfile.fa");
	}

	FastaFormat ef = new FastaFormat();
	SequenceFactory sf = new SimpleSequenceFactory();
	InputStream is = new FileInputStream(seqFile);
	    
	StreamReader sr = new StreamReader(is, ef, DNATools.getDNA().getParser("token"), sf);
	Sequence s = sr.nextSequence();

	Feature.Template ft = new Feature.Template();
	ft.annotation = Annotation.EMPTY_ANNOTATION;
	ft.location = new RangeLocation(3, 8);
	s.createFeature(ft);

	f = new JFrame("Sequence test");
	sp = new SequencePanel();
	sp.setSequence(s);
	sp.setScale(20.0);
	sp.setDirection(SequencePanel.HORIZONTAL);
	fr = new BasicFeatureRenderer();
	features = new FeatureBlockSequenceRenderer();
	features.setFeatureRenderer(fr);
	sp.addRenderer(features);
	sp.addRenderer(new SymbolSequenceRenderer());
	f.getContentPane().setLayout(new FlowLayout());
	f.getContentPane().add(sp);

	JButton vert = new JButton("Vertical");
	vert.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		sp.setDirection(SequencePanel.VERTICAL);
		f.pack();
	    }
	} );
	JButton horiz = new JButton("Horizontal");
	horiz.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		sp.setDirection(SequencePanel.HORIZONTAL);
		f.pack();
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
	f.getContentPane().add(vert);
	f.getContentPane().add(horiz);
	f.getContentPane().add(blue);
	f.getContentPane().add(red);

	f.pack();
	f.setVisible(true);
    }
}
