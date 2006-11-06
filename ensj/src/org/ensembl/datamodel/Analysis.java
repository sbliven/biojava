/*
    Copyright (C) 2002 EBI, GRL

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


package org.ensembl.datamodel;
import java.sql.Timestamp;

/**
 * Represents a process used to create features such as genes.
 */
public interface Analysis extends Persistent {
    /**
     * Logical name of the analysis. 
     */
    String getLogicalName();

    void setLogicalName(String logicalName);

    /**
     * External program used for analysis. 
     */
    String getProgram();

    void setProgram(String program);

    /**
     * External program used for analysis. 
     */
    String getProgramVersion();

    void setProgramVersion(String programVersion);

    /**
     * External program used for analysis. 
     */
    String getProgramFile();

    void setProgramFile(String programFile);

    /**
     * Source database used for analysis. 
     */
    String getParameters();

    void setParameters(String parameters);

    /**
     * Source database used for analysis. 
     */
    String getSourceDatabase();

    void setSourceDatabase(String sourceDatabase);

    /**
     * Source database used for analysis. 
     */
    String getSourceDatabaseVersion();

    void setSourceDatabaseVersion(String sourceDatabaseVersion);

    /**
     * Source database used for analysis. 
     */
    String getSourceDatabaseFile();

    void setSourceDatabaseFile(String sourceDatabaseFile);

    /**
     * Runnable used for analysis. 
     */
    String getRunnable();

    void setRunnable(String module);

    String getRunnableVersion();

    void setRunnableVersion(String runnableVersion);

    String getGFFSource();

    void setGFFSource(String source);

    String getGFFFeature();

    void setGFFFeature(String feature);

    Timestamp getCreated();

    void setCreated(Timestamp created);
}
