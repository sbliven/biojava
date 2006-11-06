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

package org.ensembl.compara.driver;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.EnsemblDriver;

/**
 * Provides adaptors for accessing an ensembl compara database.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface ComparaDriver extends EnsemblDriver {

  
  DnaDnaAlignFeatureAdaptor getDnaDnaAlignFeatureAdaptor() throws AdaptorException;
  
  DnaFragmentAdaptor getDnaFragmentAdaptor() throws AdaptorException;
  
  GenomeDBAdaptor getGenomeDBAdaptor() throws AdaptorException;
  
  GenomicAlignAdaptor getGenomicAlignAdaptor() throws AdaptorException;
  
  GenomicAlignBlockAdaptor getGenomicAlignBlockAdaptor() throws AdaptorException;
  
  HomologyAdaptor getHomologyAdaptor() throws AdaptorException;
  
  MemberAdaptor getMemberAdaptor() throws AdaptorException;
  
  MethodLinkAdaptor getMethodLinkAdaptor() throws AdaptorException;
  
  MethodLinkSpeciesSetAdaptor getMethodLinkSpeciesSetAdaptor() throws AdaptorException;
  
}
