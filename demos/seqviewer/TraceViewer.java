package seqviewer;

import java.io.*;
import java.awt.*;
import javax.swing.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.sequence.*;
import org.biojava.bio.program.abi.*;

public class TraceViewer {
  public static void main(String[] args)
  throws Exception {
    File abiFile = new File(args[0]);
    ABITrace trace = new ABITrace(abiFile);
    
    AbiTraceRenderer traceRenderer = new AbiTraceRenderer();
    traceRenderer.setTrace(trace);
    traceRenderer.setDepth(300.0);
    
    MultiLineRenderer mlr = new MultiLineRenderer();
    mlr.addRenderer(traceRenderer);
    mlr.addRenderer(new SymbolSequenceRenderer());
    mlr.addRenderer(new RulerRenderer());
    
    SequencePanel sp = new SequencePanel();
    sp.setSequence(SequenceTools.createSequence(
      trace.getSequence(),
      abiFile.toString(),
      abiFile.toString(),
      Annotation.EMPTY_ANNOTATION
    ));
    sp.setDirection(SequencePanel.HORIZONTAL);
    sp.setScale(8.0);
    sp.setRange(new RangeLocation(1, sp.getSymbols().length()));
    sp.setRenderer(mlr);

    JFrame frame = new JFrame("Trace: " + abiFile);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(new JScrollPane(sp), BorderLayout.CENTER);
    frame.setSize(800, 400);
    frame.setVisible(true);
  }
}
