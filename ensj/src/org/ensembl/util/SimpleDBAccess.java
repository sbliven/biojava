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
package org.ensembl.util;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.StringTokenizer;

public class SimpleDBAccess {

  public static final int PORT_FORWARD_TO_ACARI = 0;
  public static final int KAKA = 1;
  public static final int GEORDY = 2;

  public static Connection getConnection(String connectionLabel) {
    String connStr=null, user=null, password=null;
    
    try {
      Properties connections = new Properties();
      URL file =
        SimpleDBAccess.class.getResource("SimpleDBAccess.properties");
      connections.load(file.openStream());
      String connectionSettings = connections.getProperty(connectionLabel);


      StringTokenizer tokens = new StringTokenizer(connectionSettings, "+");
      connStr = tokens.nextToken();
      user = tokens.nextToken();
      password = (tokens.hasMoreTokens()) ? tokens.nextToken() : "";

      System.out.println("cs = " + connStr);
      System.out.println("u = " + user);
      System.out.println("p = " + password);

      return createConnection(connStr, user, password);

    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    return null;
  }

  public static Connection getConnection(int DESTINATION) {
    return getConnection(DESTINATION, "ensembl100");
  }
  public static Connection getConnection(int DESTINATION,
                                         String database) {
    Connection conn = null;
    
      String connStr = null;
      String user = null;

      switch(DESTINATION) {
        //if ( DESTINATION==PORT_FORWARD_TO_ACARI ) {
      case PORT_FORWARD_TO_ACARI:
        connStr ="jdbc:mysql://127.0.0.1:3307/" + database;
        user = "ensro";
        break;
        //} 
        //if ( DESTINATION==KAKA ) {
      case KAKA:
        connStr ="jdbc:mysql://kaka.sanger.ac.uk/" + database;
        user = "anonymous";
        //} 
        break;
      case GEORDY:
        connStr ="jdbc:mysql://geordy.ebi.ac.uk/" + database;
        user = "craig";
      }

      return createConnection(connStr, user, "");
  }


  public static Connection createConnection(String connStr,
                                            String user,
                                            String password){
    Connection conn = null;

    try {
      Class.forName("org.gjt.mm.mysql.CoreDriver").newInstance();
      conn = DriverManager.getConnection(connStr,
                                         user, password);
    }catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    return conn;
  }


  public static void main(String[] args) {
    System.out.println("Bob bob");

    try {
      Connection conn = getConnection(KAKA);
      ResultSet rs;
      rs = conn.createStatement().executeQuery("SELECT * FROM analysis");
      
      int nColumns = rs.getMetaData().getColumnCount();
      while(rs.next()) {
        System.out.print("Row:");
        for(int i=1; i<=nColumns; ++i) {
          System.out.print(rs.getString(i) + ", ");
        }
        System.out.println();
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }

    getConnection("ECS1A");
  }
}
