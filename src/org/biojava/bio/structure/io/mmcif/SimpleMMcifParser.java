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
 * created at Mar 4, 2008
 */
package org.biojava.bio.structure.io.mmcif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.io.mmcif.model.AtomSite;
import org.biojava.bio.structure.io.mmcif.model.DatabasePDBrev;
import org.biojava.bio.structure.io.mmcif.model.Entity;
import org.biojava.bio.structure.io.mmcif.model.Struct;

/** A simple mmCif file parser
 * 
 * @author Andreas Prlic
 * @since 1.6
 */
public class SimpleMMcifParser implements MMcifParser {

	List<MMcifConsumer> consumers ;

	public static final String LOOP_END = "#";
	public static final String LOOP_START = "loop_";
	public static final String FIELD_LINE = "_";
	public static final String STRING_LIMIT = ";";

	private static final char s1 = '\'';
	private static final char s2 = '\"';


	Struct struct ;

	public SimpleMMcifParser(){
		consumers = new ArrayList<MMcifConsumer>();
		struct = null;
	}

	public void addMMcifConsumer(MMcifConsumer consumer) {
		consumers.add(consumer);

	}

	public void clearConsumers() {
		consumers.clear();

	}

	public void removeMMcifConsumer(MMcifConsumer consumer) {
		consumers.remove(consumer);		
	}

	public static void main(String[] args){
		String file = "/Users/andreas/WORK/PDB/MMCIF/1gav.mmcif";
		System.out.println("parsing " + file);
		MMcifParser parser = new SimpleMMcifParser();
		MMcifConsumer consumer = new SimpleMMcifConsumer();
		parser.addMMcifConsumer(consumer);

		try {
			File f = new File(file);
			BufferedReader buf = new BufferedReader(new InputStreamReader (new FileInputStream(file)));
			parser.parse(buf);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void parse(BufferedReader buf) 
	throws IOException {
		
		triggerDocumentStart();
		
		
		// init container objects...
		struct = new Struct();
		String line = null; 

		boolean inLoop = false;
		
		List<String> loopFields = new ArrayList<String>();
		List<String> lineData   = new ArrayList<String>();
		
		String category = null;

		
		// the first line is a data_PDBCODE line, test if this looks like a mmcif file
		line = buf.readLine();
		if (!line.startsWith("data_")){
			System.err.println("this does not look like a valid MMcif file! The first line should be data_1XYZ, but is " + line);
			triggerDocumentEnd();
			return;
		}

		while ( (line = buf.readLine ()) != null ){
			//System.out.println(inLoop + " " + line);
			
			
			if ( inLoop){

				if (line.startsWith(LOOP_END)){
					// reset all data
					inLoop = false;
					lineData.clear();
					category=null;
					loopFields.clear();
					continue;

					
				}
				if ( line.startsWith(FIELD_LINE)){
					// found another field.
					String txt = line.trim();
					//System.out.println(txt);
					if ( txt.indexOf('.') > -1){

						String[] spl = txt.split("\\.");
						//System.out.println(spl.length);
						category = spl[0];
						String attribute = spl[1];
						loopFields.add(attribute);
						if ( spl.length > 2){
							System.err.println("found nested attribute, not supported, yet!");
						}	
					} else {
						category = txt;						
					}


				} else {
					// we found a data line

					lineData = processLine(line, buf, loopFields.size());
					if ( lineData.size() != loopFields.size()){
						System.err.println("did not find enough data fields...");
						
					}

					endLineChecks(category, loopFields,lineData);				
					lineData.clear();


				}

			} else {
				
				
				if ( line.startsWith(LOOP_START)){
					loopFields.clear();
					inLoop = true;
					category=null;
					lineData.clear();
					continue;
				} else if (line.startsWith(LOOP_END)){
					inLoop = false;	
					if ( category != null)
						endLineChecks(category, loopFields, lineData);
					category = null;
					loopFields.clear();
					lineData.clear();
				} else {
					
					// a boring normal line
					List<String> data = processLine(line, buf, 2);
					//System.out.println("got a single line " + data);
					String key = data.get(0);
					int pos = key.indexOf(".");
					category = key.substring(0,pos);
					String value = data.get(1);
					loopFields.add(key.substring(pos+1,key.length()));
					lineData.add(value);

					
				}
			}



		}

		if (struct != null){
			triggerStructData(struct);
		}

		triggerDocumentEnd();

	}

	private List<String> processSingleLine(String line){
		//System.out.println("processSingleLine " + line);
		List<String> data = new ArrayList<String>();

		if ( line.trim().length() == 1){
			if ( line.startsWith(STRING_LIMIT))
				return data;
		}
		boolean inString = false;
		String word = "";


		for (int i=0; i< line.length(); i++ ){

			Character c = line.charAt(i);

			if  (c == ' ') {

				if ( ! inString){
					if ( ! word.equals(""))
						data.add(word);
					word = "";
				} else {
					// we are in a string, add the space
					word += c;
				}

			} else if ( (c == s1) || (c == s2)){

				if ( inString){
					// at end of string
					if ( ! word.equals(""))
						data.add(word);
					word = "";
					inString = false;
				} else {
					// the beginning of a new string
					inString = true;
				}
			}
			else {
				word += c;
			}

		}
		if ( ! word.trim().equals(""))
			data.add(word);

		return data;

	}

	/** get the content of a cif entry
	 * 
	 * @param line
	 * @param buf
	 * @return
	 */
	private List<String> processLine(String line, 
			BufferedReader buf,
			int fieldLength)
			throws IOException{

		//System.out.println("processLine " + fieldLength + " " + line);
		// go through the line and process each character
		List<String> lineData = new ArrayList<String>();

		boolean inString = false;

		String bigWord = null;

		while ( true ){

			if ( line.startsWith(STRING_LIMIT)){
				if (! inString){

					inString = true;
					bigWord = "";
				} else {
					// the end of a word
					lineData.add(bigWord);
					bigWord = null;
					inString = false;

				}
			} else {
				if ( inString )
					//TODO: make bigWord a stringbuffer...
					bigWord += (line);
				else {
					List<String> dat = processSingleLine(line);
					//System.out.println("processSingleLIne got:" + dat);
					for (String d : dat){
						lineData.add(d);
					}
				}
			}

			//System.out.println("in process line : " + lineData.size() + " " + fieldLength);

			if ( lineData.size() > fieldLength){
				System.err.println("wrong data length ("+lineData.size()+
						") should be ("+fieldLength+") at line " + line + " " + lineData);
				throw new RuntimeException("could not parse file");
			}

			if ( lineData.size() == fieldLength)
				return lineData;


			line = buf.readLine();
			if ( line == null)
				break;
		}
		return lineData;

	}


	private void endLineChecks(String category,List<String> loopFields, List<String> lineData ){

		//System.out.println("parsed the following data: " +category + " fields: "+
		//		loopFields + " DATA: " + 
		//		lineData);
		
		if ( loopFields.size() != lineData.size()){
			throw new RuntimeException("data lenght ("+ lineData.size() +
					") != fields length ("+loopFields.size()+
					") category: " +category + " fields: "+
				loopFields + " DATA: " + 
				lineData );
		}
		
		if ( category.equals("_entity")){
			
			Entity e =  (Entity) buildObject(
					"org.biojava.bio.structure.io.mmcif.model.Entity", 
					loopFields,lineData);
			triggerNewEntity(e);

		} else if ( category.equals("_struct")){

			struct =  (Struct) buildObject(
					"org.biojava.bio.structure.io.mmcif.model.Struct",
					loopFields, lineData);		
			
		} else if ( category.equals("_atom_site")){

			AtomSite a = (AtomSite) buildObject(
					"org.biojava.bio.structure.io.mmcif.model.AtomSite",
					loopFields, lineData);

			triggerNewAtomSite(a);
		} else if ( category.equals("_database_PDB_rev")){
			DatabasePDBrev dbrev = (DatabasePDBrev) buildObject(
					"org.biojava.bio.structure.io.mmcif.model.DatabasePDBrev",
					loopFields, lineData);
			
			triggerNewDatabasePDBrev(dbrev);
		}

	}

	private void setPair(Object o, List<String> lineData){
		Class c = o.getClass();

		if (lineData.size() == 2){
			String key = lineData.get(0);
			String val = lineData.get(1);

			int dotPos = key.indexOf('.');

			if ( dotPos > -1){
				key = key.substring(dotPos+1,key.length());
			}

			String u = key.substring(0,1).toUpperCase();
			try {
				Method m = c.getMethod("set" + u + key.substring(1,key.length()) , String.class);					
				m.invoke(o,val);
			}
			catch (InvocationTargetException iex){
				iex.printStackTrace();
			}
			catch (IllegalAccessException aex){
				aex.printStackTrace();
			}
			catch( NoSuchMethodException nex){
				System.err.println("trying to set field " + key + " in "+ c.getName() + ", but not found! (value:" + val + ")");
			}
		} else {
			System.err.println("trying to set key/value pair on object " +o.getClass().getName() + " but did not find in " + lineData);
		}
	}

	private Object buildObject(String className, List<String> loopFields, List<String> lineData) {
		Object o = null;
		try {
			// build up the Entity object from the line data...
			Class c = Class.forName(className);

			o = c.newInstance();


			int pos = -1 ;
			for (String key: loopFields){
				pos++;

				String val = lineData.get(pos);
				//System.out.println(key + " " + val);
				String u = key.substring(0,1).toUpperCase();

				try {
					Method m = c.getMethod("set" + u + key.substring(1,key.length()) , String.class);					
					m.invoke(o,val);
				}
				catch( NoSuchMethodException nex){
					System.err.println("trying to set field " + key + " in "+ c.getName() +", but not found! (value:" + val + ")");
				}
			}
		} catch (InstantiationException eix){
			eix.printStackTrace();
		} catch (InvocationTargetException etx){
			etx.printStackTrace();
		} catch (IllegalAccessException eax){
			eax.printStackTrace();
		} catch (ClassNotFoundException ex){
			ex.printStackTrace();
		}
		return o;
	}



	public void triggerDocumentStart(){
		for(MMcifConsumer c : consumers){
			c.documentStart();	
		}
	}


	public void triggerNewEntity(Entity entity){
		for(MMcifConsumer c : consumers){
			c.newEntity(entity);			
		}
	}

	private void triggerStructData(Struct struct){
		for(MMcifConsumer c : consumers){
			c.setStruct(struct);			
		}
	}

	private void triggerNewAtomSite(AtomSite atom){
		for(MMcifConsumer c : consumers){
			c.newAtomSite(atom);		
		}
	}
	
	private void triggerNewDatabasePDBrev(DatabasePDBrev dbrev){
		for(MMcifConsumer c : consumers){
			c.newDatabasePDBrev(dbrev);
		}
	}
	
	public void triggerDocumentEnd(){
		for(MMcifConsumer c : consumers){
			c.documentEnd();	
		}
	}
}
