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

package CircLocTest;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.biojava.bio.symbol.*;

public class TestFrame extends JFrame {
  JPanel contentPane;
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  JLabel jLabel1 = new JLabel();
  JTextField minAField = new JTextField("1",6);
  JLabel jLabel2 = new JLabel();
  JTextField maxAField = new JTextField("100",6);
  Border border1;
  TitledBorder titledBorder1;
  Border border2;
  JLabel jLabel3 = new JLabel();
  JLabel jLabel4 = new JLabel();
  JTextField minBField = new JTextField("1",6);
  JTextField maxBField = new JTextField("100",6);
  Border border3;
  Border border4;
  JPanel jPanel3 = new JPanel();
  JButton TestButton = new JButton();
  JButton jButton2 = new JButton();
  JPanel jPanel4 = new JPanel();
  Border border5;
  GridLayout gridLayout1 = new GridLayout();
  JLabel jLabel5 = new JLabel();
  JLabel jLabel6 = new JLabel();
  JTextField aib = new JTextField();
  JTextField aeb = new JTextField();
  JTextField aub = new JTextField();
  JLabel jLabel7 = new JLabel();
  JLabel jLabel8 = new JLabel();
  JLabel jLabel9 = new JLabel();
  JLabel jLabel10 = new JLabel();
  JLabel jLabel11 = new JLabel();
  JLabel jLabel12 = new JLabel();
  JLabel jLabel13 = new JLabel();
  JLabel jLabel14 = new JLabel();
  JTextField boo = new JTextField();
  JTextField aob = new JTextField();
  JTextField aoo = new JTextField();
  JTextField acb = new JTextField();
  JTextField boa = new JTextField();
  JTextField bca = new JTextField();
  Border border6;
  Border border7;
  JPanel jPanel5 = new JPanel();
  JTextField lengthField = new JTextField("100", 6);
  String y = "yes";
  String n = "no";
  int minA = 1;
  int maxA = 100;
  int minB = 1;
  int maxB = 100;
  int length = 100;

  //Construct the frame
  public TestFrame() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  //Component initialization
  private void jbInit() throws Exception  {
    contentPane = (JPanel) this.getContentPane();
    border1 = BorderFactory.createEmptyBorder(0,10,0,0);
    titledBorder1 = new TitledBorder("");
    border2 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white,new Color(142, 142, 142)),BorderFactory.createEmptyBorder(5,5,5,5));
    border3 = BorderFactory.createEmptyBorder(0,30,0,0);
    border5 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(142, 142, 142));
    border6 = BorderFactory.createEmptyBorder(10,10,10,10);
    border7 = BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(178, 178, 178)),BorderFactory.createEmptyBorder(15,5,15,5));
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(400, 323));
    this.setTitle("Circular Location Test");
    jPanel1.setLayout(borderLayout2);
    jLabel1.setText("MinA");
    jLabel2.setBorder(border1);
    jLabel2.setText("MaxA");
    maxAField.setHorizontalAlignment(SwingConstants.RIGHT);
    minAField.setHorizontalAlignment(SwingConstants.RIGHT);
    jPanel2.setBorder(BorderFactory.createEtchedBorder());
    jLabel3.setBorder(border3);
    jLabel3.setText("MinB");
    jLabel4.setText("MaxB");
    jLabel4.setBorder(border1);
    minBField.setHorizontalAlignment(SwingConstants.RIGHT);
    maxBField.setHorizontalAlignment(SwingConstants.RIGHT);
    TestButton.setText("Test");
    TestButton.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        TestButton_actionPerformed(e);
      }
    });
    jButton2.setText("Close");
    jButton2.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        jButton2_actionPerformed(e);
      }
    });
    contentPane.setPreferredSize(new Dimension(590, 300));
    jPanel4.setLayout(gridLayout1);
    jPanel3.setBorder(BorderFactory.createEtchedBorder());
    jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel5.setText("B.overlaps(A)");
    jLabel6.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel6.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel6.setText("A.intersection(B)");
    aib.setBorder(null);
    aib.setEditable(false);
    aib.setHorizontalAlignment(SwingConstants.CENTER);
    gridLayout1.setColumns(4);
    gridLayout1.setRows(5);
    aeb.setBorder(null);
    aeb.setEditable(false);
    aeb.setText("yes");
    aeb.setHorizontalAlignment(SwingConstants.CENTER);
    aub.setBorder(null);
    aub.setEditable(false);
    aub.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel7.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel7.setText("Length");
    jLabel8.setToolTipText("");
    jLabel8.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel8.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel8.setText("A.union(B)");
    jLabel9.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel9.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel9.setText("A.equals(B)");
    jLabel10.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel10.setText("A.overlaps(B)");
    jLabel11.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel11.setText("A.contains(B)");
    jLabel12.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel12.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel12.setText("B.overlapsOrigin");
    jLabel13.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel13.setText("B.contains(A)");
    jLabel14.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel14.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel14.setText("A.overlapsOrigin");
    boo.setHorizontalAlignment(SwingConstants.CENTER);
    boo.setBorder(null);
    boo.setEditable(false);
    boo.setText("yes");
    aob.setHorizontalAlignment(SwingConstants.CENTER);
    aob.setBorder(null);
    aob.setEditable(false);
    aob.setText("yes");
    aoo.setHorizontalAlignment(SwingConstants.CENTER);
    aoo.setBorder(null);
    aoo.setEditable(false);
    aoo.setText("yes");
    acb.setHorizontalAlignment(SwingConstants.CENTER);
    acb.setBorder(null);
    acb.setEditable(false);
    acb.setText("yes");
    boa.setHorizontalAlignment(SwingConstants.CENTER);
    boa.setBorder(null);
    boa.setEditable(false);
    boa.setText("yes");
    bca.setHorizontalAlignment(SwingConstants.CENTER);
    bca.setBorder(null);
    bca.setEditable(false);
    bca.setText("yes");
    jPanel4.setBorder(border6);
    lengthField.setHorizontalAlignment(SwingConstants.RIGHT);
    jPanel1.setMinimumSize(new Dimension(500, 255));
    contentPane.add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jPanel3, BorderLayout.SOUTH);
    jPanel3.add(TestButton, null);
    jPanel3.add(jButton2, null);
    jPanel1.add(jPanel2, BorderLayout.NORTH);
    jPanel2.add(jLabel1, null);
    jPanel2.add(minAField, null);
    jPanel2.add(jLabel2, null);
    jPanel2.add(maxAField, null);
    jPanel2.add(jLabel3, null);
    jPanel2.add(minBField, null);
    jPanel2.add(jLabel4, null);
    jPanel2.add(maxBField, null);
    jPanel1.add(jPanel4, BorderLayout.CENTER);
    jPanel4.add(jLabel11, null);
    jPanel4.add(acb, null);
    jPanel4.add(jLabel14, null);
    jPanel4.add(aoo, null);
    jPanel4.add(jLabel13, null);
    jPanel4.add(bca, null);
    jPanel4.add(jLabel12, null);
    jPanel4.add(boo, null);
    jPanel4.add(jLabel10, null);
    jPanel4.add(aob, null);
    jPanel4.add(jLabel9, null);
    jPanel4.add(aeb, null);
    jPanel4.add(jLabel5, null);
    jPanel4.add(boa, null);
    jPanel4.add(jLabel8, null);
    jPanel4.add(aub, null);
    jPanel4.add(jLabel7, null);
    jPanel4.add(jPanel5, null);
    jPanel5.add(lengthField, null);
    jPanel4.add(jLabel6, null);
    jPanel4.add(aib, null);
  }

  //Overridden so we can exit when window is closed
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      System.exit(0);
    }
  }

  void TestButton_actionPerformed(ActionEvent e) {
    try{
      minA = Integer.parseInt(minAField.getText());
      maxA = Integer.parseInt(maxAField.getText());
      minB = Integer.parseInt(minBField.getText());
      maxB = Integer.parseInt(maxBField.getText());
      if(minA==0||maxA==0||minB==0||maxB==0) throw new NumberFormatException();
    }catch(NumberFormatException ne){
      JOptionPane.showMessageDialog(this,
                                    "min and max values must non-zero integers",
                                    "Input Error",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    try{
      length = Integer.parseInt(lengthField.getText());
      if(length < 1)throw new NumberFormatException();
    }catch(NumberFormatException nfe){
      JOptionPane.showMessageDialog(this,
                                    "Length must be a Real number",
                                    "Input Error",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    CircularLocation a = LocationTools.makeCircularLocation(minA, maxA, length);
    CircularLocation b = LocationTools.makeCircularLocation(minB, maxB, length);
    test(a,b);
  }

  void jButton2_actionPerformed(ActionEvent e) {
    System.exit(0);
  }

  private void test(CircularLocation a, CircularLocation b){
      minAField.setText(String.valueOf(a.getMin()));
      maxAField.setText(String.valueOf(a.getMax()));
      minBField.setText(String.valueOf(b.getMin()));
      maxBField.setText(String.valueOf(b.getMax()));

      if(a.contains(b)){acb.setText(y);}
      else{acb.setText(n);}
      if(b.contains(a)){bca.setText(y);}
      else{bca.setText(n);}

      if(a.equals(b)) aeb.setText(y);
      else aeb.setText(n);

      aub.setText(LocationTools.union(a,b).toString());

      if(a.overlaps(b)) aob.setText(y);
      else aob.setText(n);
      if(b.overlaps(a)) boa.setText(y);
      else boa.setText(n);

      if(a.overlapsOrigin())aoo.setText(y);
      else aoo.setText(n);
      if(b.overlapsOrigin()) boo.setText(y);
      else boo.setText(n);

      aib.setText((LocationTools.intersection(a,b)).toString());
    }
}
