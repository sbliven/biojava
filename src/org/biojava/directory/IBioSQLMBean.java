import java.io.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;


public interface IBioSQLMBean {


    /**
     * Getters and setters for the database backended SeqDB
     */

    public String getDBUser();

    public void setDBUser(String dbUser);
    
    public String getDBURL();
    
    public void setDBURL(String dbURL);

    public String getDBPass();
    
    public void setDBPass(String dbPass);

    public String getDBInstanceName();

    public void setDBInstanceName(String dbInstancename);

}
