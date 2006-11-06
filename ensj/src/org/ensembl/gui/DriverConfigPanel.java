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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.ensembl.util.PropertiesUtil;

/**
 * GUI component providing user with ability to choose configuration files
 * for initialising an ensembl driver.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @version $Revision$ */
public class DriverConfigPanel extends JPanel implements ActionListener {


  private static final long serialVersionUID = 1L;
  
  private FileChooserWithHistory serverConfigChooser;
  private FileChooserWithHistory driverConfigChooser;
  private JTextField summary;



  public DriverConfigPanel (){
    TitledBorder border = new TitledBorder(new LineBorder(Color.gray), "CoreDriver");
    border.setTitleColor(Color.black);
    setBorder(border);

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    serverConfigChooser = new FileChooserWithHistory("Server", new Vector(), this);
    driverConfigChooser = new FileChooserWithHistory("Database", new Vector(), this);

    summary = new JTextField(getSummary());
    summary.setForeground(Color.black);
    summary.setBackground(Color.white);
    summary.setEditable(false);
    summary.setColumns(45);
    JPanel summaryBox = new JPanel();
    summaryBox.setLayout(new FlowLayout(FlowLayout.LEFT));
    summaryBox.add(summary);
    

    add(summaryBox);
    //add(summary);
    add(serverConfigChooser);
    add(driverConfigChooser);

    //setPreferredSize(new Dimension(400, 150));
  }


  public String getServer() {
    return serverConfigChooser.getSelected();
  }
    
  public Vector getServerHistory() {
    return serverConfigChooser.getHistory();
  }

  public void setServerHistory(Vector items) {
    serverConfigChooser.setHistory(items);
  }



  public String getDriver() {
    return driverConfigChooser.getSelected();
  }

  public Vector getDriverHistory() {
    return driverConfigChooser.getHistory();
  }

  public void setDriverHistory(Vector items) {
    driverConfigChooser.setHistory(items);
  }



  /**
   * Updates summary.
   */
  public void actionPerformed(ActionEvent ae) {
    summary.setText(getSummary());
  }



  /*
   * @return summary information extracted from config files.  */
  public String getSummary() {
    String host =  "UNKOWN HOST";
    String port =  "";
    String prefix = "jdbc:mysql://";
    String serverFile = getServer();
    
    if ( serverFile!=null ) {
      host = PropertiesUtil.getProperty(serverFile, "host");
      port = PropertiesUtil.getProperty(serverFile, "port");
      if ( port!=null ) port = ":" + port;
      else port = "";
    }



    String databaseName = "UNKOWN DATABASE";
    String driverFile = getDriver();
    if ( driverFile!=null ) {
      databaseName = PropertiesUtil.getProperty(driverFile,
                                                "database");
    }
    return prefix+host + port + "/" + databaseName;
  }
    
    


  /**
   * Shows this settings panel in a modal dialog box. Saves settings if OK is
   * pressed, otherwise ignores any choices the user makes.
   * @param parentComponent component to centre dialog box relative to.
   * @return DialogUtil.OK_OPTION if ok was selected, DialogUtil.CANCEL_OPTION if cancel
   * selected, DialogUtil.CLOSED_OPTION if user closed the dialog.  */
  public Object showDialog(Component parentComponent) {

    // copy original settings so we can reinstate them later if the user
    // makes and then hits cancel or close.
    Vector oldServerHistory = (Vector)serverConfigChooser.getHistory().clone();
    Vector oldDriverHistory = (Vector)driverConfigChooser.getHistory().clone();


    Object option = DialogUtil.showOkCancelDialog(this,
                                                  parentComponent,
                                                  "Adapter Settings");

    // Copy old values back into data model.
    if ( option!=DialogUtil.OK_OPTION ) {
      serverConfigChooser.setHistory(oldServerHistory);
      driverConfigChooser.setHistory(oldDriverHistory);
    }
 
    return option;
  }




  public static void main(String[] args) {

    DriverConfigPanel g= new DriverConfigPanel();
    Vector serverItems = new Vector();
    Vector driverItems = new Vector();

    String base = "/home/craig/";
    
    serverItems.add(base+"dev/ensj-core/resources/data/ecs1d_mysql_server.properties");
    serverItems.add(base+"dev/ensj-core/resources/data/ecs1h_mysql_server.properties");
    serverItems.add(base+"dev/ensj-core/resources/data/kaka_mysql_server.properties");
    serverItems.add(base+"dev/ensj-core/resources/data/localhost_mysql_server.properties");
    serverItems.add(base+"dev/ensj-core/resources/data/localhost_mysql_server_3307.properties");
    g.setServerHistory(serverItems);

    driverItems.add(base+"dev/ensj-core/resources/data/current_driver.properties");
    driverItems.add(base+"dev/ensj-core/resources/data/homo_sapiens_core_120_driver.properties");
    driverItems.add(base+"dev/ensj-core/resources/data/homo_sapiens_core_3_26_driver.properties");
    driverItems.add(base+"dev/ensj-core/resources/data/homo_sapiens_core_4_28_driver.properties");
    driverItems.add(base+"dev/ensj-core/resources/data/mus_musculus_core_4_1_driver.properties");
    driverItems.add(base+"dev/ensj-core/resources/data/mus_musculus_core_4_2_driver.properties");
    g.setDriverHistory(driverItems);

    if ( args.length>0 && args[0].equals("-d") ) {
      Object option = g.showDialog(null);
      System.out.println(option);
      System.exit(0);
    }
    else {
      JFrame f = new JFrame("EnsJAdaptorGUI");
      f.getContentPane().add(g);
      f.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            System.exit(0);
          }});
      f.pack();
      f.setVisible(true);
    }
  }



    
}// DriverConfigPanel
