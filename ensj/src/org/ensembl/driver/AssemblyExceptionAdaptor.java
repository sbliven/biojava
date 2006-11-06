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
package org.ensembl.driver;

import java.util.List;

import org.ensembl.datamodel.AssemblyException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SequenceRegion;

/**
 * Adaptor for retrieving and handling AssemblyExceptions which are used to
 * represent PARs and haplotypes.
 * 
 * <p>
 * PAR/haplotype sequences can be thought of as series of sequence region
 * chunks. Some of these chunks are specific to the PAR/Haplotype and others are
 * links to parts of "reference" chromosomes. e.g. DR52 consists of sequence
 * regions unique to DR52 and chunks of reference chromosome 6.
 * </p>
 * 
 * <p>
 * As an optimisation Ensembl databases do not store duplicate sequence and
 * features for the regions PAR/haplotypes share with "reference" chromosomes.
 * Instead they store the links between these chunks as AssemblyLocations. This
 * information can be used to retrieving sequence and features for the
 * PARs/Haplotypes that is stored "on" the the reference sequence.
 * </p>
 * 
 * <p>
 * A location on a shared region of a PAR/Haplotype can be converted between the
 * PAR/Haplotype and reference chromosomes via dereference(Location) and
 * reference(Location, String).
 * </p>
 * 
 * @see org.ensembl.datamodel.AssemblyException
 * @see #dereference(Location)
 * @see #rereference(Location, SequenceRegion)
 */
public interface AssemblyExceptionAdaptor extends Adaptor {

  final static String TYPE = "assembly_exception";

  /**
   * Retrieve all AssemblyExceptions.
   * 
   * @return all AssemblyExceptions
   * @throws AdaptorException
   */
  List fetch() throws AdaptorException;

  /**
   * Retrieve AssemblyException with specified internalID.
   * 
   * @param internalID
   *          internal ID of the assembly location
   * @return AssemblyException assembly exception with specified internal ID or
   *         null if none found.
   * @throws AdaptorException
   */
  AssemblyException fetch(long internalID) throws AdaptorException;

  /**
   * Retrieve AssemblyExceptions that are relevant for given location.
   * 
   * <p>
   * They have to be on the same sequence region with their linked Location.
   * </p>
   * 
   * @param loc
   * @return zero or more AssemblyExceptions that overlap the given location
   * @throws AdaptorException
   */
  List fetch(Location loc) throws AdaptorException;

  /**
   * Dereferences the request location.
   * 
   * Features for some seq regions , e.g. human chromosome:DR51, are stored in
   * chunks where some of the chunks are actually references to other sequence
   * regions e.g. chromosome:6.
   * 
   * 
   * e.g. chromosome:DR51 -> [chromosome:6:1-b, chromosome:DR51:1-c,
   * chromosome:6:d-end]
   * 
   * @param loc
   *          location (list) to be dereferenced.
   * @return location (list) corresponding to loc containing one or more
   * nodes, or null if no mapping is possible.
   * 
   * @throws AdaptorException
   * @see #rereference(Location, SequenceRegion)
   * @see #hasReferences(Location)
   */
  Location dereference(Location loc) throws AdaptorException;

  /**
   * Refereferences the source locations.
   * 
   * Maps the locations into the specified coordinate system and target sequence
   * name if possible.
   * 
   * e.g. [chromosome:6:1-b, chromosome:DR51:1-c, chromosome:6:d-end] ->
   * chromosome:DR51
   * 
   * @param loc
   *          source location (list) to be rereferenced
   * @param targetSeqRegion
   *          sequence region to convert locations into (if necessary)
   * @return location (list) on targetSeqRegion, or null if
   *         a mapping is not possible.
   * @throws AdaptorException
   * @see #hasReferences(Location)
   */
  Location rereference(Location loc, SequenceRegion targetSeqRegion)
      throws AdaptorException;

  /**
   * Determines whether loc includes references to other locations.
   * 
   * @param loc
   *          location to test.
   * @return true if loc contains references, otherwise false.
   * @throws AdaptorException
   */
  boolean hasReferences(Location loc) throws AdaptorException;

}
