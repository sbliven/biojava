/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.idmapping;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

//-------------------------------------------------------------------------
/**
 * Hold information about a scored pair.
 */

public class Entry implements Serializable,Cloneable {

  private static final long serialVersionUID = 1L;

	public long source, target;
	
	/**
	 * In the scoredMappingMatrix is used to build source and target linked lists
	 */
	public long nextSourceForTarget, nextTargetForSource;
    public float score;

    public Entry(long source, long target, float score) {

        this.source = source;
        this.target = target;
        this.score = score;

    }

    public Entry copy() {
      try {
        return (Entry) clone();
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
    
    public String toString() {

        return "[Entry: source=" + source + " target=" + target + " score=" + score + "]";

    }

    /**
     * @return Returns the score.
     */
    public float getScore() {

        return score;
    }

    /**
     * @param score The score to set.
     */
    public void setScore(float score) {

        this.score = score;
    }

    /**
     * @return Returns the source.
     */
    public long getSource() {

        return source;
    }

    /**
     * @param source The source to set.
     */
    public void setSource(long source) {

        this.source = source;
    }

    /**
     * @return Returns the target.
     */
    public long getTarget() {

        return target;
    }

    /**
     * @param target The target to set.
     */
    public void setTarget(long target) {

        this.target = target;
    }

    public boolean equals(Object o) {

        if (!(o instanceof Entry)) {
            return false;
        }

        Entry e = (Entry) o;
        boolean scoresEqual = false;
        if (Math.abs(score - e.getScore()) < 0.000001f) {
            scoresEqual = true;
        }
        return (source == e.getSource() && target == e.getTarget() && scoresEqual);

    }

    public int hashCode() {

        int hash = 17;

        hash = 37 * hash * (int) source;

        hash = 37 * hash * (int) target;

        hash += (int) score;

        return hash;
    }

    /**
     * Writes all entries to filename.
     * 
     * @param entries zero or more Entry elements.
     * @param fileName output filename.
     */
    public static void writeToFile(Collection entries, String fileName) {
    
    	try {
    
    		Iterator it = entries.iterator();
    
    		OutputStreamWriter writer = new OutputStreamWriter(
    				new FileOutputStream( fileName));
    
    		while (it.hasNext()) {
    
    			Entry entry = (Entry) it.next();
    			writer.write(entry.getSource() + "\t" + entry.getTarget()
    					+ "\t" + entry.score + "\n");
    		}
    		writer.close();
    
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

} // Entry

/**
 * Compare 2 Entry objects on the basis of the internal IDs of their source objects.
 */

class EntrySourceComparator implements Comparator {

	private EntryTargetComparator e;
	
	public EntrySourceComparator( EntryTargetComparator e ) {
		this.e = e;
	}
	
	public EntrySourceComparator() {
		super();
		e = null;
	}
	
    public int compare(Object arg0, Object arg1) {

        long id0 = ((Entry) arg0).getSource();
        long id1 = ((Entry) arg1).getSource();

        if (id0 > id1) {
            return 1;
        } else if (id0 < id1) {
            return -1;
        } else {
        		if( e != null ) {
        			return e.compare( arg0, arg1 );
        		} else {
        			return 0;
        		}
        }

    }

} // EntrySourceComparator

//-------------------------------------------------------------------------

/**
 * Compare 2 Entry objects on the basis of the internal IDs of their target objects.
 */

class EntryTargetComparator implements Comparator {

	private EntrySourceComparator esc;
	
	public EntryTargetComparator() {
		super();
	}
	
	public EntryTargetComparator( EntrySourceComparator esc ) {
		this.esc = esc;
	}
 
	public int compare(Object arg0, Object arg1) {

        long id0 = ((Entry) arg0).getTarget();
        long id1 = ((Entry) arg1).getTarget();

        if (id0 > id1) {
            return 1;
        } else if (id0 < id1) {
            return -1;
        } else {
        		if( esc != null ) {
        			return esc.compare( arg0, arg1 );
        		} else {
        			return 0;
        		}
        }

    }

} // EntryTargetComparator

//-------------------------------------------------------------------------

/**
 * Compare 2 Entry objects on the basis of their scores, <em>lowest</em> score first. If scores
 * are equal, entries are compared on the basis of their source and target IDs. Hence the compare()
 * method will only return 0 if the scores, sources and targets are all equal.
 */

class EntryScoreComparator implements Comparator {

    Comparator sourceComp = new EntrySourceComparator();

    Comparator targetComp = new EntryTargetComparator();

    public int compare(Object arg0, Object arg1) {

        float s0 = ((Entry) arg0).getScore();
        float s1 = ((Entry) arg1).getScore();

        if (s0 > s1) {
            return 1;
        } else if (s0 < s1) {
            return -1;
        } else {

            // scores equal, so compare on source & target
            int sourceResult = sourceComp.compare(arg0, arg1);
            if (sourceResult != 0) {
                return sourceResult;
            } else {
                return targetComp.compare(arg0, arg1);
            }
        }

    }
} // EntryScoreComparator

/**
 * Compare 2 Entry objects on the basis of their scores, <em>highest</em> score first. If scores
 * are equal, entries are compared on the basis of their source and target IDs. Hence the compare()
 * method will only return 0 if the scores, sources and targets are all equal.
 */

class EntryScoreReverseComparator implements Comparator {

    Comparator esc = new EntryScoreComparator();
    
    public int compare(Object arg0, Object arg1) {
      
        return -esc.compare(arg0, arg1);
        
    }
    
} // EntryScoreReverseComparator

//-------------------------------------------------------------------------

