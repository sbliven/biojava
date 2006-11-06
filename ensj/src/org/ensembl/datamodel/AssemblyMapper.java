/*
 * Created on 20-Oct-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.ensembl.datamodel;

/**
 * @author arne
 *
 */

import org.ensembl.driver.AdaptorException;
import org.ensembl.util.mapper.Coordinate;

public interface AssemblyMapper {
	public Coordinate[] map(Location loc) throws AdaptorException;
	public void flush();
	public int getSize();
	public Coordinate fastmap(Location loc) throws AdaptorException;
	
}
