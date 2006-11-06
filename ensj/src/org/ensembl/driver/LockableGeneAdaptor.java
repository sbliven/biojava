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

import org.ensembl.datamodel.Gene;

/**
 * When the implementing class overrides store() the intention is that it will succeed only if the gene is locked by the 'current' user and that the lock is released before any other user can modify the gene.
 */
public interface LockableGeneAdaptor extends GeneAdaptor {
    void lock(Gene gene) throws AdaptorException;

    void release(Gene gene) throws AdaptorException;
}
