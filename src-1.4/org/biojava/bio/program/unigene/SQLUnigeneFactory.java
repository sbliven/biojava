package org.biojava.bio.program.unigene;

import java.io.*;
import java.net.*;
import java.sql.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

class SQLUnigeneFactory
implements UnigeneFactory {
  private static String CREATE_DB_STATEMENT;

  private static String getCreateDBStatement() {
    if(CREATE_DB_STATEMENT == null) {
      StringBuffer stmt = new StringBuffer();
      BufferedReader stmtIn = new BufferedReader(
        new InputStreamReader(
          SQLUnigeneFactory.class.getClassLoader().getResourceAsStream(
            "/org/biojava/bio/program/unigene/createUnigene.sql"
          )
        )
      );
    }
    return CREATE_DB_STATEMENT;
  }

  public boolean canAccept(URL dbURL) {
    return dbURL.getProtocol().equals("jdbc");
  }

  public UnigeneDB loadUnigene(URL dbURL)
  throws BioException {
    if(!canAccept(dbURL)) {
      throw new BioException("Can't resolve url to an sql unigene db: " + dbURL);
    }

    JDBCConnectionPool conPool = new JDBCConnectionPool(dbURL.toString());

    return new SQLUnigeneDB(conPool);
  }

  public UnigeneDB createUnigene(URL dbURL)
  throws BioException {
    String dbString = dbURL.toString();
    int lastSlash = dbString.lastIndexOf("/");
    String rootURL = dbString.substring(0, lastSlash);
    String dbName = dbString.substring(lastSlash + 1);

    JDBCConnectionPool connPool = new JDBCConnectionPool(rootURL);

    Statement stmt = null;
    try {
      stmt = connPool.takeStatement();
      stmt.execute("create database " + dbName);
      stmt.execute("use " + dbName);
      stmt.execute(getCreateDBStatement());
    } catch (SQLException se) {
      throw new BioException(se, "Could not create database");
    } finally {
      try {
        connPool.putStatement(stmt);
      } catch (SQLException se) {
        // not much we can do about this
      }
    }

    return new SQLUnigeneDB(connPool);
  }
}
