import java.util.*;
import java.io.*;
import java.net.*;

// RI imports
//
import com.sun.management.jmx.Trace;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanParameterInfo;

import javax.management.MalformedObjectNameException;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;


public class BioSQLAgent {
   
    private MBeanServer server = null;
    
    public BioSQLAgent(){
	
	//create the mbean server
	server =  MBeanServerFactory.createMBeanServer();
    }

    public void init(){
	
	ObjectName mbeanObjectName = null;
	String domain = server.getDefaultDomain();
	String mbeanName = "BioSQLMBean";
	try {
	    mbeanObjectName = new ObjectName(domain + ":type=" + mbeanName);
	}catch(MalformedObjectNameException e) {
	    echo("\t!!! Could not create the MBean ObjectName !!!");
	    e.printStackTrace();
	    echo("\nEXITING...\n");
	    System.exit(1);
	}
	
	try{
	    
	    server.createMBean(mbeanName,mbeanObjectName);
	}catch(Exception e) {
	    echo("\t!!! Could not create the " + mbeanName + " MBean !!!");
	    e.printStackTrace();
	    echo("\nEXITING...\n");
	    System.exit(1);
	}
    }
	
    
    public void manageMBean(ObjectName objectName){

	Attribute dbUserAttr = new Attribute("DBUser", "");
	Attribute dbURLAttr = new Attribute("DBURL", "");
	Attribute dbPassAttr = new Attribute("DBPass","");
	Attribute dbInstanceNameAttr = new Attribute("DBInstanceName", "");
	
	//This seems dumb hopefully I can di this in batch mode??
	server.setAttribute(objectName, dbUserAttr);
	server.setAttribute(objectName, dbURLAttr);
	server.setAttribute(dbObjectName, dbPassAttr);
	server.setAttribute(dbObjectName, dbInstanceNameAttr);
	
	
    }

    public void printBeanInfo(ObjectName objectName){

	try {
	    System.out.println("\n    Getting attribute values:");
	    System.out.println((String) server.getAttribute(mbeanObjectName,"DBURL"));
	    
	} catch (Exception e) {
	    echo("\t!!! Could not read attributes !!!");
	    e.printStackTrace();
	    
	}
    }

    
}
