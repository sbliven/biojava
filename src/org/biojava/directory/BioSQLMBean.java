import java.io.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;


public class BioSQLMBean implements IBioSQLMBean{

    private String dbURL = null;
    private String dbUser = null;
    private String dbPass = null;
    private String dbInstanceName = null;

    public BioSQLMBean(){}


    public String getDBUser(){

	return dbUser;
    }

    public void setDBUser(String dbUser){
	this.dbUser = dbUser;
    }

    public String getDBURL(){
	return dbURL;
    }

    public void setDBURL(String URL){
	this.dbURL = dbURL;
    }
    
    public String getDBPass(){
	return dbPass;
    }

    public void setDBPass(String DBPass){
	this.dbPass = dbPass;
    }

    public String getDBInstanceName(){
	return dbInstanceName;
    }

    public void setDBInstanceName(String dbInstanceName){
	this.dbInstanceName = dbInstanceName;
    }
}
