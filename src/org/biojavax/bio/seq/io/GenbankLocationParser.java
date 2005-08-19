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

package org.biojavax.bio.seq.io;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.biojava.bio.seq.io.ParseException;
import org.biojavax.CrossRef;
import org.biojavax.Namespace;
import org.biojavax.SimpleCrossRef;
import org.biojavax.bio.db.RichObjectFactory;
import org.biojavax.bio.seq.CompoundRichLocation;
import org.biojavax.bio.seq.Position;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichLocation.Strand;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.SimpleRichLocation;
import org.biojavax.ontology.ComparableTerm;


/**
 *
 * @author Richard Holland
 */
public class GenbankLocationParser {
    // No instances please
    private GenbankLocationParser() {}
    
    public static RichLocation parseLocation(Namespace featureNS, String featureAccession, String locationString) throws ParseException {
        /*
         *
3.5.3 Location examples
The following is a list of common location descriptors with their meanings:
Location                  Description
         
467                       Points to a single base in the presented sequence
         
340..565                  Points to a continuous range of bases bounded by and
                          including the starting and ending bases
         
<345..500                 Indicates that the exact lower boundary point of a
                          feature is unknown.  The location begins at some
                          base previous to the first base specified (which need
                          not be contained in the presented sequence) and con-
                          tinues to and includes the ending base
         
<1..888                   The feature starts before the first sequenced base
                          and continues to and includes base 888
         
(102.110)                 Indicates that the exact location is unknown but that
                          it is one of the bases between bases 102 and 110, in-
                          clusive
         
(23.45)..600              Specifies that the starting point is one of the bases
                          between bases 23 and 45, inclusive, and the end point
                          is base 600
         
(122.133)..(204.221)      The feature starts at a base between 122 and 133,
                          inclusive, and ends at a base between 204 and 221,
                          inclusive
         
123^124                   Points to a site between bases 123 and 124
         
145^177                   Points to a site between two adjacent bases anywhere
                          between bases 145 and 177
         
join(12..78,134..202)     Regions 12 to 78 and 134 to 202 should be joined to
                          form one contiguous sequence
         
complement(1..23)         Complements region 1 to 23
         
complement(join(2691..4571,4918..5163)
                          Joins regions 2691 to 4571 and 4918 to 5163, then
                          complements the joined segments (the feature is
                          on the strand complementary to the presented strand)
         
join(complement(4918..5163),complement(2691..4571))
                          Complements regions 4918 to 5163 and 2691 to 4571,
                          then joins the complemented segments (the feature is
                          on the strand complementary to the presented strand)
         
complement(34..(122.126)) Start at one of the bases complementary to those
                          between 122 and 126 on the presented strand and finish
                          at the base complementary to base 34 (the feature is
                          on the strand complementary to the presented strand)
         
J00194:100..202           Points to bases 100 to 202, inclusive, in the entry
                          (in this database) with primary accession number
                          'J00194'
         */
        rank = 1;
        return parseLocString(featureNS, featureAccession, null, Strand.POSITIVE_STRAND, locationString);
    }
    
    // O beautiful regex, we worship you.
    private static Pattern gp = Pattern.compile("^([^\\(\\):]*?:)?(complement|join|order)?\\(*{0,1}(.*?)\\)*{0,1}$");
    private static Pattern rp = Pattern.compile("^\\(*(.*?)\\)*(\\.\\.\\(*(.*)\\)*)?$");
    private static Pattern xp = Pattern.compile("^(.*?)(\\.(\\d+))?:$");
    private static Pattern pp = Pattern.compile("^\\(*(<|>)?(\\d+)(([\\.\\^])(\\d+))?(<|>)?\\)*$");
    private static int rank;
    private static RichLocation parseLocString(Namespace featureNS, String featureAccession, CrossRef parentXref, Strand parentStrand, String locStr) throws ParseException {
        Matcher gm = gp.matcher(locStr);
        if (!gm.matches()) throw new ParseException("Bad location string found: "+locStr);
        String xrefName = gm.group(1);
        String groupType = gm.group(2);
        String subLocStr = gm.group(3);
        
        CrossRef crossRef = parentXref;
        if (xrefName!=null) {
            Matcher xm = xp.matcher(xrefName);
            if (!xm.matches()) throw new ParseException("Bad location xref found: "+xrefName);
            String xrefAccession = xm.group(1);
            String xrefVersion = xm.group(3);
            if (xrefAccession.equals(featureAccession)) crossRef = null;
            else {
                if (xrefVersion!=null) {
                    crossRef = (SimpleCrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{
                        featureNS.getName(),xrefAccession,Integer.valueOf(xrefVersion)
                    });
                } else {
                    crossRef = (SimpleCrossRef)RichObjectFactory.getObject(SimpleCrossRef.class,new Object[]{
                        featureNS.getName(),xrefAccession
                    });
                }
            }
        }
        
        Strand strand = parentStrand;
        if (groupType!=null) {
            if (groupType.equals("complement")) {
                // It's a complement location
                if (parentStrand==Strand.NEGATIVE_STRAND) strand = Strand.POSITIVE_STRAND;
                else strand = Strand.NEGATIVE_STRAND;
                return parseLocString(featureNS,featureAccession,crossRef,strand,subLocStr);
            } else {
                // It's a compound location.
                ComparableTerm groupTypeTerm = null;
                if (groupType.equals("order")) groupTypeTerm = CompoundRichLocation.getOrderTerm();
                else if (groupType.equals("join")) groupTypeTerm = CompoundRichLocation.getJoinTerm();
                else throw new ParseException("Unknown group type "+groupType+" received");
                
                // recurse on each block and return the compounded result
                List members = new ArrayList();
                
                StringBuffer sb = new StringBuffer();
                char[] chars = subLocStr.toCharArray();
                int bracketCount = 0;
                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    if (c=='(') bracketCount++;
                    else if (c==')') bracketCount--;
                    if (c==',' && bracketCount==0) {
                        String subStr = sb.toString();
                        members.add(parseLocString(featureNS,featureAccession,crossRef,parentStrand,subStr));
                        sb.setLength(0); // reset buffer
                    } else sb.append(c);
                }
                if (sb.length()>0) members.add(parseLocString(featureNS,featureAccession,crossRef,parentStrand,sb.toString()));
                
                if (members.size()>1) {
                    // Normal case where join() or order() wrap multiple locations.
                    return new CompoundRichLocation(groupTypeTerm, members);
                } else if (members.size()==1) {
                    // Dodgy case where join() or order() only wrap a single locations.
                    return (RichLocation)members.get(0);
                } else {
                    // Really dodgy case with no members at all!
                    throw new ParseException("Group found with no members. Term: "+groupType);
                }
            }
        }
        
        // Process a simple location.
        Matcher rm = rp.matcher(subLocStr);
        if (!rm.matches()) throw new ParseException("Bad location description found: "+subLocStr);
        String start = rm.group(1);
        String end = rm.group(3);
        
        Position startPos = parsePosition(start);        
        if (end==null) {
            // A point location
            return new SimpleRichLocation(startPos,startPos,rank++,strand,crossRef);
        } else {
            // A range location
            Position endPos = parsePosition(end);
            return new SimpleRichLocation(startPos,endPos,rank++,strand,crossRef);
        }
    }
    
    private static Position parsePosition(String position) throws ParseException {
        Matcher pm = pp.matcher(position);
        if (!pm.matches()) throw new ParseException("Could not understand position: "+position);
        String startfuzz = pm.group(1);
        String endfuzz = pm.group(6);
        boolean endStartsFuzzy = ((startfuzz!=null && startfuzz.equals("<")) || (endfuzz!=null && endfuzz.equals("<")));
        boolean endEndsFuzzy = ((endfuzz!=null && endfuzz.equals(">")) || (startfuzz!=null && startfuzz.equals(">")));
        String endStart = pm.group(2);
        String endRangeType = pm.group(4);
        String endEnd = pm.group(5);
        Position endPos = null;
        if (endRangeType!=null) {
            // fuzziest
            return new SimplePosition(endStartsFuzzy,endEndsFuzzy,Integer.parseInt(endStart),Integer.parseInt(endEnd),endRangeType);
        } else {
            // less fuzzy
            return new SimplePosition(endStartsFuzzy,endEndsFuzzy,Integer.parseInt(endStart));
        }
    }
    
    public static String writeLocation(RichLocation l) {
        //write out location text
        //use crossrefs to calculate remote location positions
        //one big join (or order?) with complemented parts
        if (l instanceof CompoundRichLocation) {
            return _writeGroupLocation(l.blockIterator(),l.getTerm());
        } else {
            return _writeSingleLocation(l);
        }
    }
    
    private static String _writePosition(Position p) {
        StringBuffer sb = new StringBuffer();
        int s = p.getStart();
        int e = p.getEnd();
        String t = p.getType();
        boolean fs = p.hasFuzzyStart();
        boolean fe = p.hasFuzzyEnd();
        if (t!=null) {
            // a range - put in brackets
            sb.append("(");
            if (fs) sb.append("<");
            sb.append(s);
            sb.append(t);
            sb.append(e);
            if (fe) sb.append(">");
            sb.append(")");
        } else {
            // not a range - no brackets
            if (fs) sb.append("<");
            sb.append(s);
            if (fe) sb.append(">");
        }
        return sb.toString();
    }
    
    private static String _writeSingleLocation(RichLocation l) {
        StringBuffer loc = new StringBuffer();
        loc.append(_writePosition(l.getMinPosition()));
        if (!l.getMinPosition().equals(l.getMaxPosition())) {
            loc.append("..");
            loc.append(_writePosition(l.getMaxPosition()));
        }
        if (l.getStrand()==Strand.NEGATIVE_STRAND) {
            loc.insert(0,"complement(");
            loc.append(")");
        }
        if (l.getCrossRef()!=null) {
            loc.insert(0,":");
            int version = l.getCrossRef().getVersion();
            if (version!=0) {
                loc.insert(0,version);
                loc.insert(0,".");
            }
            loc.insert(0,l.getCrossRef().getAccession());
        }
        return loc.toString();
    }
    
    private static String _writeGroupLocation(Iterator i, ComparableTerm parentTerm) {
        StringBuffer sb = new StringBuffer();
        sb.append(parentTerm.getName());
        sb.append("(");
        while (i.hasNext()) {
            RichLocation l = (RichLocation)i.next();
            if (l instanceof CompoundRichLocation) {
                sb.append(_writeGroupLocation(l.blockIterator(),l.getTerm()));
            } else {
                sb.append(_writeSingleLocation(l));
            }
            if (i.hasNext()) sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }
}
