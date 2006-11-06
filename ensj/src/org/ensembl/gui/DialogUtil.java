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

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * DialogUtil.java
 *
 *
 * Created: Tue Apr 23 12:53:32 2002
 *
 * @author <a href="mailto: "Craig Melsopp</a>
 */

public class DialogUtil {

  public final static String OK_OPTION = "Ok";
  public final static String CANCEL_OPTION = "Cancel";
  public final static String CLOSED_OPTION = "Closed";
  

  /**
   * Show _component_ in a modal dialog box with "ok" and "cancel" options.
   * @param component component to be displayed int dialog box.
   * @param parentComponent parent component for centering dialog.
   * @param title dialog title
   * @return OK_OPTION, CANCEL_OPTION or CLOSED_OPTION if window closed.
   */
  public static Object showOkCancelDialog(Component component, 
                                          Component parentComponent,
                                          String title) {
    JOptionPane p = new JOptionPane(component
                                    ,JOptionPane.PLAIN_MESSAGE
                                    ,JOptionPane.OK_CANCEL_OPTION
                                    ,null
                                    ,new Object[] {OK_OPTION, CANCEL_OPTION});

    p.createDialog(parentComponent, title).show();
    Object option = p.getValue();
    if ( option==null ) option = CLOSED_OPTION;

    return option;
  }

}// DialogUtil
