/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.symbol;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;

/**
 * <code>FuzzyPointLocation</code>.
 *
 * I wouldn't use this yet. I've just typed it in one go, so the logic
 * may be wrong. It isn't used in the parser yet.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public class FuzzyPointLocation implements Location, Serializable
{
    public final static PointResolver RESOLVE_MIN;
    public final static PointResolver RESOLVE_MAX;
    public final static PointResolver RESOLVE_AVERAGE;

    static
    {
	RESOLVE_MIN     = new MinPointResolver();
	RESOLVE_MAX     = new MaxPointResolver();
	RESOLVE_AVERAGE = new AveragePointResolver();
    }

    private int           min;
    private int           max;
    private PointResolver resolver;

    public FuzzyPointLocation(int min, int max, PointResolver resolver)
	throws BioException
    {
	if ((min == Integer.MIN_VALUE) || (min == Integer.MAX_VALUE))
	    if ((max == Integer.MIN_VALUE) || (max == Integer.MAX_VALUE))
		throw new BioException("A fuzzy point may only have an unbounded max OR min"); 

	if ((max == Integer.MIN_VALUE) || (max == Integer.MAX_VALUE))
	    if ((min == Integer.MIN_VALUE) || (min == Integer.MAX_VALUE))
		throw new BioException("A fuzzy point may only have an unbounded max OR min"); 
	    
	this.min      = min;
	this.max      = max;
	this.resolver = resolver;
    }

    public int getMin()
    {
	return resolver.resolve(this);
    }

    public int getMax()
    {
	return resolver.resolve(this);
    }


    public boolean hasBoundedMin()
    {
	return min != Integer.MIN_VALUE;
    }
  
    public boolean hasBoundedMax()
    {
	return max != Integer.MAX_VALUE;
    }

    public boolean overlaps(Location loc)
    {
	return loc.contains(this);
    }

    public boolean contains(Location loc)
    {
	return (hasBoundedMin() && hasBoundedMax()) &&
	   (resolver.resolve(this) == loc.getMin()) &&
	   (resolver.resolve(this) == loc.getMax()); 
    }

    public boolean contains(int point)
    {
	return resolver.resolve(this) == point;
    }

    public boolean equals(Location loc)
    {
	return this.contains(loc) && loc.contains(this);
    }

    public Location intersection(Location loc)
    {
	return loc.contains(this)
	    ? this
	    : Location.empty;
    }

    public Location union(Location loc)
    {
	List locations = new ArrayList();
	locations.add(this);
	locations.add(loc);
	CompoundLocation cl = new CompoundLocation(locations);
	return cl;
    }

    public SymbolList symbols(SymbolList slist)
    {
	final Symbol sym = slist.symbolAt(resolver.resolve(this));
	try
	{
	    return new SimpleSymbolList(slist.getAlphabet(), new AbstractList()
		{
		    public Object get(int index)
			throws IndexOutOfBoundsException
		    {
			if (index == 0)
			{
			    return sym;
			}

			throw new IndexOutOfBoundsException("Index " + index + " greater than 0");
		    }

		    public int size()
		    {
			return 1;
		    }
		});
	}
	catch (IllegalSymbolException ex)
	{
	    throw new BioError(ex);
	}
    }

    public boolean isContiguous()
    {
	return true;
    }

    public Iterator blockIterator()
    {
	return Collections.singleton(this).iterator();
    }
  
    public Location translate(int dist)
    {
	if (dist == 0)
	    return this;

	try
	{
	    return new FuzzyPointLocation(this.min + dist,
					  this.max + dist,
					  this.resolver);
	}
	catch (BioException bex)
	{
	    bex.printStackTrace();
	}

	return this;
    }

    public String toString()
    {
	if (hasBoundedMin() && hasBoundedMax())
	{
	    return "["
		+ Integer.toString(getMin())
		+ "."
		+ Integer.toString(getMax());
	}
	else if (hasBoundedMin())
	{
	    return "[>"
		+ Integer.toString(getMin())
		+ "]";
	}
	else
	{
	    return "[<"
		+ Integer.toString(getMax())
		+ "]";
	}
    }

    public static interface PointResolver
    {
	public int resolve(FuzzyPointLocation loc);
    }

    private static class MinPointResolver implements PointResolver
    {
	public int resolve(FuzzyPointLocation loc)
	{
	    if (loc.hasBoundedMin())
		return loc.getMin();
	    else
		return Integer.MIN_VALUE;
	}
    }

    private static class MaxPointResolver implements PointResolver
    {
	public int resolve(FuzzyPointLocation loc)
	{
	    if (loc.hasBoundedMax())
		return loc.getMax();
	    else
		return Integer.MAX_VALUE;
	}
    }

    private static class AveragePointResolver implements PointResolver
    {
	public int resolve(FuzzyPointLocation loc)
	{
	    // Range of form: (123.567)
	    if (loc.hasBoundedMin() && loc.hasBoundedMax())
	    {
		return loc.getMin() + loc.getMax() / 2;
	    }
	    // Range of form: <123 or >123
	    else
	    {
		return loc.hasBoundedMin() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
	    }
	}
    }
}
