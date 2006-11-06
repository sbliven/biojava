/*
  Copyright (C) 2003 EBI, GRL

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.ensembl.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;


/**
 * GUI widget from which user can select logging configuration. Supports
 * history via a drop-down list. 
 *
 *
 * Created: Tue Apr 23 11:21:29 2002
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @version <$Revision$>
 */

public class LoggingConfPanel extends JPanel implements ActionListener {

  private static final long serialVersionUID = 1L;
	
	private JRadioButton defaultRButton;
  private JRadioButton customRButton;
  private JLabel title;
  private JButton customButton;

  private FileChooserWithHistory customFileChooser;

  private String defaultLoggingFile;

  public final static String DEFAULT_LOGGING_FILE = "resources/data/logging_info_level.properties";

  public LoggingConfPanel (){
    defaultLoggingFile = DEFAULT_LOGGING_FILE;

    TitledBorder border = new TitledBorder(new LineBorder(Color.gray), "Logging");
    border.setTitleColor(Color.black);
    setBorder(border);

    defaultRButton = new JRadioButton("Default");
    defaultRButton.setSelected(true);
    customRButton = new JRadioButton("Custom");
    customButton = new JButton("Choose");
    customButton.addActionListener(this);
    

    ButtonGroup bg = new ButtonGroup();
    bg.add(defaultRButton);
    bg.add(customRButton);

    Vector v = new Vector();
    customFileChooser = new FileChooserWithHistory("Logging", v, null);

    add(defaultRButton);
    add(customRButton);
    add(customButton);

  }


  public String getDefaultLoggingFile() {
    return defaultLoggingFile;
  }


  public void setDefaultLoggingFile(String file) {
    defaultLoggingFile = file;
  }


  public void actionPerformed(ActionEvent ae) {

    if ( ae.getSource()==customButton ) {
      Object option = DialogUtil.showOkCancelDialog(customFileChooser, 
                                                    this, 
                                                    "Custom Logging Configuration File");
      if ( option==DialogUtil.OK_OPTION ) {
        customRButton.setSelected(true);
      }
    }
    
  }
 


  public void setCustomHistory(Vector initialHistory) {
    customFileChooser.setHistory(initialHistory);
  }



  public Vector getCustomHistory() {
    return customFileChooser.getHistory();
  }
  

  public void setDefault(boolean v) {
    defaultRButton.setSelected(v);
    customRButton.setSelected(!v);
  }

  public boolean isDefault() {
    return defaultRButton.isSelected();
  }


   
  public String getSelected() {
    if ( defaultRButton.isSelected() ) return DEFAULT_LOGGING_FILE;
    else return customFileChooser.getSelected();
  }



  public static void main(String[] args){

    LoggingConfPanel lcp = new LoggingConfPanel();

    JFrame f = new JFrame("Logging Conf Panel");
    f.getContentPane().add(lcp);
    f.pack();
    f.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
    
    f.setVisible(true);

  }
} // LoggingConfPanel
