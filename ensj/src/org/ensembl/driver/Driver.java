/*
	Copyright (C) 2005 EBI, GRL

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

package org.ensembl.driver;

/**
 * A compatibility interface that allows legacy user code to work unmodified
 * even though Driver had been replaced by CoreDriver.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @deprecated Deprecated in version 29.2, use org.ensembl.driver.CoreDriver instead.
 */
public interface Driver extends CoreDriver {

}
