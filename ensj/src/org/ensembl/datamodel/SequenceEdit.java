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

package org.ensembl.datamodel;

/**
 * A sequence modification action.
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public interface SequenceEdit {

  /**
   * Code for this sequence edit.
   * @return Code for this sequence edit.
   */
  String getCode();

  /**
   * Name of this sequence edit.
   * @return name of this sequence edit.
   */
   String getName();

  /**
   * Description for this sequence edit.
   * @return description for this sequence edit.
   */
  String getDescription();

  /**
   * The start position of the region replaced by the alternative sequence.
   *
   * Coordinates are inclusive and one-based, which means that
   * inserts are unusually represented by a start 1bp higher than
   * the end.
   * 
   *  E.g. start = 1, end = 1 is a replacement of the first base but
   *  start = 1, end = 0 is an insert BEFORE the first base.
   * @return The start position of the region replaced by the alternative sequence.
   */
  int getStart();

  /**
   * The end position of the region replaced by the alternative sequence.
   *
   * Coordinates are inclusive and one-based, which means that
   * inserts are unusually represented by a start 1bp higher than
   * the end.
   * 
   *  E.g. start = 1, end = 1 is a replacement of the first base but
   *  start = 1, end = 0 is an insert BEFORE the first base.
   * @return The end position of the region replaced by the alternative sequence.
   */
  int getEnd();

  /**
   * The replacement sequence used by this edit.
   * 
   * The sequence may either be a string of amino acids or
   *  nucleotides depending on the context in which this edit is
   *  used.
   *
   * In the case of a deletion the replacement sequence is an empty
   * string.
   * @return The replacement sequence used by this edit.
   */
  String getAlternativeSequence();

  /**
   * Creates a new sequence that is the original with this sequence
   * applied to to it.
   * @param sourceSequence source sequence.
   * @return modified sequence.
   */
  String applyEdit(String sourceSequence);


  /**
   * The difference in length caused by applying this
   * edit to a sequence.  
   * @return The difference in length caused by applying this
   * edit to a sequence. This may be be negative (deletion),
   * positive (insertion) or 0 (replacement).  If either start or end are not defined 0 is returned.
   */
  int lengthDiff();
}
