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

import java.awt.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.acedb.*;

public class AceTreeView {
    public static void main(String[] args) throws Exception {
      try {
	if (args.length != 6)
	    throw new RuntimeException("AceTreeView hostname port username class object");

	String host = args[0];
	String port = args[1];
	String user = args[2];
	String passwd = args[3];
	String clazzName = args[4];
	String objName = args[5];

	Ace.registerDriver(new org.acedb.socket.SocketDriver());
	URL _dbURL = new URL("acedb://" + host + ":" + port);
  AceURL dbURL = new AceURL(_dbURL, user, passwd, null);
	AceURL seqURL = dbURL.relativeURL(clazzName + "/" + objName);
	AceObject seqObj = (AceObject) Ace.fetch(seqURL);
	AceTreeModel tm = new AceTreeModel(seqObj);

	JFrame myFrame = new JFrame("ACeDB " + clazzName + " " + objName);
	JTree myTree = new JTree();
	myTree.setModel(tm);
	myTree.setCellRenderer(new AceNodeNameRenderer());
	myTree.setEditable(false);
	myFrame.getContentPane().add(new JScrollPane(myTree));
	myFrame.pack();
	myFrame.setVisible(true);
	System.out.println("Going...");
    } catch (Throwable t) {
      t.printStackTrace();
    }
    }
    
}


class AceNodeNameRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree,
						   Object value,
						   boolean sel,
						   boolean expand,
						   boolean leaf,
						   int row,
						   boolean hasFocus) {
	return super.getTreeCellRendererComponent(tree, 
						  ((AceNode) value).getName(),
						  sel, expand, leaf, row,
						  hasFocus);
    }
}


class AceTreeModel implements TreeModel {
    private AceObject obj;

    public AceTreeModel(AceObject obj) {
	this.obj = obj;
    }

    public Object getRoot() {
	return obj;
    }

    public Object getChild(Object parent, int indx) {
      try {
	AceNode p = (AceNode) parent;
	if (indx >= p.size())
	    return null;
	Iterator i = p.iterator();
	while ((indx--) > 0)
	    i.next();
	return i.next();
      } catch (AceException ae) {
        throw new AceError(ae, "Pants");
      }
    }

    public int getChildCount(Object parent) {
      try {
	return ((AceSet) parent).size();
      } catch (AceException ae) {
        throw new AceError(ae, "Pants");
      }
    }

    public boolean isLeaf(Object parent) {
	return (getChildCount(parent) == 0);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public int getIndexOfChild(Object parent, Object child) {
      try {
	int i = 0;
	Iterator it = ((AceSet) parent).iterator();
	while (it.hasNext() && (it.next() != child))
	    ++i;
	return i;
      } catch (AceException ae) {
        throw new AceError(ae, "Pants");
      }
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }
}
