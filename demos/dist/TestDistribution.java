package dist;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.gui.*;

public class TestDistribution {
  private static Distribution nullModel;
  public static void main(String [] args) {
    try {
    FiniteAlphabet dna = DNATools.getDNA();
    nullModel = new UniformDistribution(dna);
    final Distribution dist = DistributionFactory.DEFAULT.createDistribution(dna);
    randomize(dist);
    
    final DistributionLogo sLogo = new DistributionLogo();
    sLogo.setDistribution(dist);
    sLogo.setLogoPainter(new TextLogoPainter());
    sLogo.setStyle(new DNAStyle());
    Action randomizeAction = new AbstractAction("Randomize") {
      public void actionPerformed(ActionEvent ae) {
        randomize(dist);
        sLogo.repaint();
      }
    };
    JButton button = new JButton("Randomize");
    button.addActionListener(randomizeAction);
    
    JFrame f = new JFrame("Distribution Logo");
    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(BorderLayout.CENTER, sLogo);
    f.getContentPane().add(BorderLayout.SOUTH, button);
    
    f.setSize(300, 300);
    f.setVisible(true);
  } catch (Throwable t) {
    t.printStackTrace();
  }
  }
  
  private static void randomize(Distribution dist) {
    try {
      DistributionTrainerContext dtc = new SimpleDistributionTrainerContext();
      dtc.registerDistribution(dist);
      for(Iterator i = ((FiniteAlphabet) dist.getAlphabet()).iterator(); i.hasNext(); ) {
        dtc.addCount(dist, (Symbol) i.next(), Math.random());
      }
      dtc.train();
    } catch (IllegalSymbolException ise) {
      throw new BioError(ise, "This should be impossible");
    } catch (ChangeVetoException cve) {
      throw new BioError(cve, "This should be impossible");
    }
  }
}
