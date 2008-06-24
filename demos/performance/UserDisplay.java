/*
 *                  BioJava development code
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
 * Created on Jun 20, 2008
 * 
 */

package performance;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.Box;
import javax.swing.JEditorPane;
import javax.swing.JFrame;

import javax.swing.JScrollPane;

public class UserDisplay extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	JEditorPane body;
	JScrollPane scroll;
	Box vBox;
	
	public UserDisplay(){
		super();
	
		body = new JEditorPane("text/html", "");
		
		body.setEditable(false);
		scroll = new JScrollPane(body);
		vBox = Box.createVerticalBox();
		vBox.add(scroll);
		
		this.getContentPane().add(vBox);
		
		this.setSize(new Dimension(400,500));
		this.addWindowStateListener(new WindowStateListener(){

			public void windowStateChanged(WindowEvent e) {
				// TODO Auto-generated method stub
				System.out.println("window state changed");
				System.exit(0);
			}});
		
		this.addWindowListener(new WindowListener (){

			
			public void windowClosing(WindowEvent e) {				
				System.exit(0);
				// 
				
			}

			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			
		}
		);

	}

	public void setText(String text){
		body.setText(text);
		body.repaint();
		body.revalidate();
		this.repaint();
		
	}


}
