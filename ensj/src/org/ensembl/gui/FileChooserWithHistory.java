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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;


/**
 * File chooser with history list. Defaults to home directory if items is empty. Emits
 * an ActionEvent if the value changes. 
 */
  public class FileChooserWithHistory extends Box implements ActionListener {

  private static final long serialVersionUID = 1L;
	
	private Vector history;
  private JComponent parent;
  private JComboBox historyList;

  private javax.swing.filechooser.FileFilter dotConfFilter;
  private String preferredExtension;

  /**
   * Simple record used to present file names instead of file paths in 
   * _list_. We do this by overriding the toString() method.
   */
  private class FileName {

    String fullPath;
    String name;

    FileName(String fullPath) {
      this.fullPath = fullPath;
      this.name = new File(fullPath).getName();
    }

    public String toString() {
      return name;
    }
  }


    public FileChooserWithHistory(
      String label,
      Vector initialHistory,
      JComponent parent
    ){
      this(
        label,
        initialHistory,
        parent,
        "conf"
      );
    }
    
    
    public FileChooserWithHistory(
      String label,
      Vector initialHistory,
      JComponent parent,
      String extension
    ){
      super(BoxLayout.X_AXIS);

      this.parent = parent;

      history = new Vector();
      historyList = new JComboBox();
      historyList.setModel(new DefaultComboBoxModel(history));
      historyList.setPreferredSize(new Dimension(200, 50));
      setHistory(initialHistory);

      JButton browseButton = new JButton("browse");
      browseButton.addActionListener(this);

      if(label != null){
        add(new JLabel(label));
      }//end if

      add(Box.createHorizontalStrut(5));
      add(historyList);
      add(Box.createHorizontalStrut(5));
      add(browseButton);
      add(Box.createHorizontalGlue());

      dotConfFilter = 
        new ExtensionFileFilter(
          extension, 
          extension+" files (*."+extension+")"
        );

      // This is a slightly cheeky way of passing change events to the parent
      // class. The parent should really register itself as a listener to
      // this class but that would mean making this inner class capable of
      // emitting events. This requires a lot more code.
      if (parent!=null
          && parent instanceof ActionListener) historyList.addActionListener((ActionListener)parent);

  }

  public String getSelected() {
    if ( history.size()>0 && historyList.getSelectedItem() != null){
      return ((FileName)historyList.getSelectedItem()).fullPath;
    } else {
      return null;
    }//end if
  }//end getSelected

  public void setSelected(String inputName) {
    FileName selectedName = null;
    Iterator fileNames = null;
    if(inputName != null){
      fileNames = history.iterator();
      while(
        fileNames.hasNext() &&
        (
          selectedName == null ||
          !selectedName.toString().equals(inputName)
        )
      ){
        selectedName = (FileName)fileNames.next();
      }
      historyList.getModel().setSelectedItem(selectedName);
    }
  }//end setSelected

  /**
   * @return vector of filepath strings.
   */
  public Vector getHistory() {

    Vector filePaths = new Vector(history.size());
    Iterator iter = history.iterator();
    while( iter.hasNext() ) {
      filePaths.add(((FileName)iter.next()).fullPath);
    }

    return filePaths;
  }


  /**
   * @param history vector of filepath strings.
   */
  public void setHistory(Vector history) {
    // Create a history list of FileName objects. One for each history item.
    Vector fileNames = new Vector(history.size());
    Iterator iter = history.iterator();
    while( iter.hasNext() ) {
      FileName fn = new FileName((String)iter.next());
      fileNames.add(fn);
    }

    this.history.clear();
    this.history.addAll(fileNames);
    if ( historyList.getItemCount()>0  ) historyList.setSelectedIndex(0);
  }



  public void actionPerformed(ActionEvent ae) {

    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(dotConfFilter);

      
    String currentPath = getSelected();
    File file = null;
    if ( currentPath!=null ) file =  new File(currentPath);
    else file = chooser.getCurrentDirectory();
    chooser.setSelectedFile(file);

    int option = chooser.showOpenDialog(parent);
    if(option == JFileChooser.APPROVE_OPTION) {
      String filepath = chooser.getSelectedFile().getAbsolutePath();
      if ( currentPath==null || !currentPath.equals(filepath) ) {
        historyList.insertItemAt(new FileName(filepath), 0);
        historyList.setSelectedIndex(0);
      }
    }

  }
  
  private String getPreferredExtension(){
    return preferredExtension;
  }
  
}
