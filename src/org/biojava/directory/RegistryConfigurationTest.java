package org.biojava.directory;

import java.io.*;
import java.util.*;


public class RegistryConfigurationTest{

    public static void main(String[] args){

	try{
	    RegistryConfiguration conf = new RegistryConfiguration("/Users/gilmanb/Desktop/biojava-live/src/org/biojava/directory/seqdatabase.ini");

	    HashMap config = conf.getConfiguration();
	    
	    System.out.println(config.get("EMBL"));
	    
	    
	}catch(FileNotFoundException fnfe){
	    fnfe.printStackTrace();
	}catch(IOException ioe){
	    ioe.printStackTrace();
	}
    }
}
