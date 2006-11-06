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


/**
 * Alignment to external reference. 
 */
public interface UngappedAlignment {
  /**
   * Location in ensembl database.
   */
  Location getLocation();

  /**
   * Location in ensembl database.
   */
  void setLocation(Location location);

  ExternalRef getExternalRef();

  void setExternalRef(ExternalRef externalRef);

  /**
   * End position in externalReference dna.
   */
  int getExternalEnd();

  /**
   * End position in externalReference dna.
   */
  void setExternalEnd(int externalEnd);

  /**
   * Strand in externalReference dna.
   */
  int getExternalStrand();

  /**
   * Strand in externalReference dna.
   */
  void setExternalStrand(int externalStrand);

  /**
   * Start in externalReference dna.
   */
  int getExternalStart();

  /**
   * Start in externalReference dna.
   */
  void setExternalStart(int externalStart);

  /** @link dependency */
  /*# ExternalRef lnkExternalRef; */
}
