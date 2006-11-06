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
package org.ensembl.datamodel.impl;

import java.sql.Timestamp;

import org.ensembl.datamodel.Analysis;
import org.ensembl.driver.CoreDriver;

public class AnalysisImpl extends PersistentImpl implements Analysis {

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;


	private String logicalName;
	private String program;
    private String programVersion;
	private String runnableVersion;
	private String database;
	private String module;
	private String source;
	private String feature;


	/**
	 *  Constructor for the AnalysisImpl object
	 */
	public AnalysisImpl() { }

    public AnalysisImpl( CoreDriver driver) {
        super( driver );
    }

	/**
	 *  Sets the logicalName attribute of the AnalysisImpl object
	 *
	 *@param  logicalName  The new logicalName value
	 */
	public void setLogicalName(String logicalName) {
		this.logicalName = logicalName;
	}


	/**
	 *  Sets the program attribute of the AnalysisImpl object
	 *
	 *@param  program  The new program value
	 */
	public void setProgram(String program) {
		this.program = program;
	}


	/**
	 *  Sets the runnableVersion attribute of the AnalysisImpl object
	 *
	 *@param  runnableVersion  The new runnableVersion value
	 */
	public void setRunnableVersion(String runnableVersion) {
		this.runnableVersion = runnableVersion;
	}


	/**
	 *  Sets the sourceDatabase attribute of the AnalysisImpl object
	 *
	 *@param  database  The new sourceDatabase value
	 */
	public void setSourceDatabase(String database) { this.sourceDatabase = database; }


	/**
	 *  Sets the runnable attribute of the AnalysisImpl object
	 *
	 *@param  module  The new runnable value
	 */
	public void setRunnable(String module) { this.runnable = module; }


	/**
	 *  Sets the gFFSource attribute of the AnalysisImpl object
	 *
	 *@param  source  The new gFFSource value
	 */
	public void setGFFSource(String source) { this.GFFSource = source; }


	/**
	 *  Sets the gFFFeature attribute of the AnalysisImpl object
	 *
	 *@param  feature  The new gFFFeature value
	 */
	public void setGFFFeature(String feature) { this.GFFFeature = feature; }

	/**
	 *  Logical name of the analysis.
	 *
	 *@return    The logicalName value
	 */
	public String getLogicalName() {
		return logicalName;
	}


	/**
	 *  External program used for analysis.
	 *
	 *@return    The program value
	 */
	public String getProgram() {
		return program;
	}

    public String getProgramVersion(){ return programVersion; }

    public void setProgramVersion(String programVersion){ this.programVersion = programVersion; }

	/**
	 *  Gets the runnableVersion attribute of the AnalysisImpl object
	 *
	 *@return    The runnableVersion value
	 */
	public String getRunnableVersion() {
		return runnableVersion;
	}

    private String sourceDatabase;

	/**
	 *  Source database used for analysis.
	 *
	 *@return    The sourceDatabase value
	 */
	public String getSourceDatabase() { return sourceDatabase; }

    private String runnable;

	/**
	 *  Runnable used for analysis.
	 *
	 *@return    The runnable value
	 */
	public String getRunnable() { return runnable; }

    private String GFFSource;

	/**
	 *  Gets the gFFSource attribute of the AnalysisImpl object
	 *
	 *@return    The gFFSource value
	 */
	public String getGFFSource() { return GFFSource; }

    private String GFFFeature;

	/**
	 *  Gets the gFFFeature attribute of the AnalysisImpl object
	 *
	 *@return    The gFFFeature value
	 */
	public String getGFFFeature() { return GFFFeature; }

    public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append("internalID=").append(internalID).append(", ");
    buf.append("logicalName=").append(logicalName).append(", ");
    buf.append("program=").append(program).append(", ");
    buf.append("runnableVersion=").append(runnableVersion).append(", ");
    buf.append("database=").append(database).append(", ");
    buf.append("module=").append(module).append(", ");
    buf.append("source=").append(source).append(", ");
    buf.append("feature=").append(feature);
    buf.append("]");

    return buf.toString();
  }

    private String programFile;

    public String getProgramFile(){ return programFile; }

    public void setProgramFile(String programFile){ this.programFile = programFile; }

    private String parameters;

    public String getParameters(){ return parameters; }

    public void setParameters(String parameters){ this.parameters = parameters; }

    private String sourceDatabaseVersion;

    public String getSourceDatabaseVersion(){ return sourceDatabaseVersion; }

    public void setSourceDatabaseVersion(String sourceDatabaseVersion){ this.sourceDatabaseVersion = sourceDatabaseVersion; }

    private String sourceDatabaseFile;

    public String getSourceDatabaseFile(){ return sourceDatabaseFile; }

    public void setSourceDatabaseFile(String sourceDatabaseFile){ this.sourceDatabaseFile = sourceDatabaseFile; }

    private Timestamp created;

    public Timestamp getCreated(){ return created; }

    public void setCreated(Timestamp created){ this.created = created; }
}

