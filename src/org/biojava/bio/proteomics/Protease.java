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

package org.biojava.bio.proteomics;


import com.sun.xml.parser.Resolver;
import com.sun.xml.tree.XmlDocument;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.io.IOException;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.net.URL;
import java.util.ArrayList;


/** The protease class stores parameters needed by Digest to digest a protein sequence.
 * A custom protease can be created or one derived from the attributes set in the
 * ProteaseManager.xml resource.
 * @author Michael Jones
 */
public class Protease {
    
    static URL proteaseManagerURL;
    
    static {
        try {
            proteaseManagerURL = Protease.class.getClassLoader().getResource(
                "org/biojava/bio/proteomics/ProteaseManager.xml"
            );  
            //If I try and do this here on compile it says "An exception can't be thrown by an initializer"
            //InputSource is = Resolver.createInputSource(proteaseManagerURL, true);
            //Document doc = XmlDocument.createXmlDocument(is, true);
        }catch (MissingResourceException mre) {
            System.err.println(mre.getMessage());
        }
    }
    
    public static String TRYPSIN = "Trypsin";
    public static String LYS_C = "Lys-C";
    public static String Arg_C = "Arg-C";
    public static String ASP_N = "Asp-N";
    public static String GLU_C_BICARB = "Glu-C-bicarbonate";
    public static String GLU_C_PHOS = "Glu-C-phosphate";
    public static String CHYMOTRYP = "Chymostrypsin";
    public static String CNBr = "CNBr"; 
    
    private static String cleavageResidues = "";
    private static String notCleaveResidues = "";
    private static boolean endoProtease = true;
    
    /** Creates new Protease */
    public Protease(String cleavageResidues, boolean endoProtease, String notCleaveResidues) {
        this.cleavageResidues = cleavageResidues;
        this.endoProtease = endoProtease;
        this.notCleaveResidues = notCleaveResidues;
    }
    
    /** Creates new Protease */
    public Protease(String cleavageResidues, boolean endoProtease) {
        this.cleavageResidues = cleavageResidues;
        this.endoProtease = endoProtease;
        this.notCleaveResidues = "";
    }
    
    public Protease() {

    }
    
    public String getCleaveageResidues()
    {
        return cleavageResidues;
    }
    
    public String getNotCleaveResidues()
    {
        return notCleaveResidues;
    }
    
    public boolean isEndoProtease()
    {
        return endoProtease;
    }
    
    
    /** Get the list of proteases defined in the ProteaseManager.xml file.
     * @return An array of protease names
     */
    public static String[] getProteaseList(){
        ArrayList list = new ArrayList();
        try{
            InputSource is = Resolver.createInputSource(proteaseManagerURL, true);
            Document doc = XmlDocument.createXmlDocument(is, true);
       
            NodeList children = doc.getDocumentElement().getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
                Element child = (Element) children.item(i);
                if(child.getNodeName().equals("protease")) {
                    String name = child.getAttribute("name");
                    list.add(name);
                }
            }
        }catch(IOException ioe){ioe.printStackTrace();}
        catch(SAXException sax){sax.printStackTrace();}
        String[] names = new String[list.size()];
        return (String[])list.toArray(names);
    }
    
    /** Creates a protease instance based on the parameters defined in the
     * ProteaseManager.xml
     * @param proteaseName A protease name that is defined in the ProteaseManager.xml
     * @return A protease instance for the given protease name.
     */
    public static Protease getProteaseByName(String proteaseName){
        Protease protease = null;
        try{
            InputSource is = Resolver.createInputSource(proteaseManagerURL, true);
            Document doc = XmlDocument.createXmlDocument(is, true);
            
            NodeList children = doc.getDocumentElement().getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
                Element child = (Element) children.item(i);
                if(child.getNodeName().equals("protease")) {
                    if(child.getAttribute("name").equals(proteaseName))
                    {
                        //Parameters
                        String cleavRes = null;
                        String exceptRes = null;
                        boolean endo = false;
                        NodeList proteaseNodes = child.getChildNodes();
                        for(int j = 0; j < proteaseNodes.getLength(); j++)
                        {
                            Element el = (Element) proteaseNodes.item(j);
                            String name = el.getNodeName();
                            String content = el.getFirstChild().getNodeValue();
                            if(name.equals("cleaveRes")) {
                                cleavRes = content;
                            }else if(name.equals("exceptRes")) {
                                exceptRes = content;
                            }else if(name.equals("endo")) {
                                endo = new Boolean(content).booleanValue();
                            }
                        }
                        if(cleavRes != null && exceptRes != null){
                            protease = new Protease(cleavRes ,endo, exceptRes);
                        }else if(cleavRes != null && exceptRes == null){
                            protease = new Protease(cleavRes ,endo);
                        }
                    }
                }
            }
        }catch(IOException ioe){ioe.printStackTrace();}
        catch(SAXException sax){sax.printStackTrace();}
        return protease;
    }        
}