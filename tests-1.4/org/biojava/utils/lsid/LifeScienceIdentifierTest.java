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

package org.biojava.utils.lsid;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import junit.framework.TestCase;

/**
 * Test LifeScienceIdentifier.
 *
 * @author Michael Heuer
 */
public class LifeScienceIdentifierTest
    extends TestCase
{

    public LifeScienceIdentifierTest(String name)
    {
	super(name);
    }

    public void testValueOfParameters()
    {
	LifeScienceIdentifier lsid = LifeScienceIdentifier.valueOf("authority",
								   "namespace",
								   "objectId",
								   0,
								   "security");
	assertNotNull(lsid);
	assertTrue("authority".equals(lsid.getAuthority()));
	assertTrue("namespace".equals(lsid.getNamespace()));
	assertTrue("objectId".equals(lsid.getObjectId()));
	assertTrue(0 == lsid.getVersion());
	assertTrue("security".equals(lsid.getSecurity()));
    }
    public void testValueOfParametersNullSecurity()
    {
	LifeScienceIdentifier lsid = LifeScienceIdentifier.valueOf("authority",
								   "namespace",
								   "objectId",
								   0,
								   null);
	assertNotNull(lsid);
	assertTrue("authority".equals(lsid.getAuthority()));
	assertTrue("namespace".equals(lsid.getNamespace()));
	assertTrue("objectId".equals(lsid.getObjectId()));
	assertTrue(0 == lsid.getVersion());
	assertTrue(lsid.getSecurity() == null);
    }
    public void testValueOfParametersEmptySecurity()
    {
	LifeScienceIdentifier lsid = LifeScienceIdentifier.valueOf("authority",
								   "namespace",
								   "objectId",
								   0,
								   "");
	assertNotNull(lsid);
	assertTrue("authority".equals(lsid.getAuthority()));
	assertTrue("namespace".equals(lsid.getNamespace()));
	assertTrue("objectId".equals(lsid.getObjectId()));
	assertTrue(0 == lsid.getVersion());
	assertTrue("".equals(lsid.getSecurity()));
    }
    public void testValueOfString()
    {
	try
	{
	    LifeScienceIdentifier lsid = LifeScienceIdentifier.valueOf("authority:namespace:objectId:0:security");

	    assertNotNull(lsid);
	    assertTrue("authority".equals(lsid.getAuthority()));
	    assertTrue("namespace".equals(lsid.getNamespace()));
	    assertTrue("objectId".equals(lsid.getObjectId()));
	    assertTrue(0 == lsid.getVersion());
	    assertTrue("security".equals(lsid.getSecurity()));
	}
	catch (LifeScienceIdentifierParseException e)
	{
	    fail(e.getMessage());
	}
    }
    public void testValueOfStringNullSecurity()
    {
	try
	{
	    LifeScienceIdentifier lsid = LifeScienceIdentifier.valueOf("authority:namespace:objectId:0");

	    assertNotNull(lsid);
	    assertTrue("authority".equals(lsid.getAuthority()));
	    assertTrue("namespace".equals(lsid.getNamespace()));
	    assertTrue("objectId".equals(lsid.getObjectId()));
	    assertTrue(0 == lsid.getVersion());
	    assertTrue(lsid.getSecurity() == null);
	}
	catch (LifeScienceIdentifierParseException e)
	{
	    fail(e.getMessage());
	}
    }
    public void testValueOfStringEmptySecurity()
    {
	try
	{
	    LifeScienceIdentifier lsid = LifeScienceIdentifier.valueOf("authority:namespace:objectId:0:");

	    assertNotNull(lsid);
	    assertTrue("authority".equals(lsid.getAuthority()));
	    assertTrue("namespace".equals(lsid.getNamespace()));
	    assertTrue("objectId".equals(lsid.getObjectId()));
	    assertTrue(0 == lsid.getVersion());
	    assertTrue("".equals(lsid.getSecurity()));
	}
	catch (LifeScienceIdentifierParseException e)
	{
	    fail(e.getMessage());
	}
    }

    private boolean throwsException(String value)
    {
	try
	{
	    LifeScienceIdentifier lsid = LifeScienceIdentifier.valueOf(value);
	}
	catch (LifeScienceIdentifierParseException e)
	{
	    return true;
	}

	return false;
    }

    public void testParseErrors()
    {
	List parseErrors = Arrays.asList(new String[] {"",
						       "authority:",
						       "authority:namespace:",
						       "authority:namespace:objectId:",
						       "authority:namespace:objectId:not_a_number",
						       "authority:namespace:objectId:1.2345"});

	for (Iterator i = parseErrors.iterator(); i.hasNext(); )
	{
	    String value = (String) i.next();
	    if (! (throwsException(value)))
	    {
		fail("expected LifeScienceIdentifierParseException for: " + value);
	    }
	}
    }
    public void testObjectContract()
    {
	LifeScienceIdentifier lsid0 = LifeScienceIdentifier.valueOf("authority",
								    "namespace",
								    "objectId",
								    0,
								    "security");

	LifeScienceIdentifier lsid1 = LifeScienceIdentifier.valueOf("authority",
								    "namespace",
								    "objectId",
								    0,
								    "security");

	LifeScienceIdentifier lsid2 = LifeScienceIdentifier.valueOf("authority",
								    "namespace",
								    "objectId",
								    1,
								    "security");

	LifeScienceIdentifier lsid3 = LifeScienceIdentifier.valueOf("authority1",
								    "namespace",
								    "objectId",
								    0,
								    "security");

	LifeScienceIdentifier lsid4 = LifeScienceIdentifier.valueOf("authority",
								    "namespace",
								    "objectId",
								    0,
								    null);

	LifeScienceIdentifier lsid5 = LifeScienceIdentifier.valueOf("authority",
								    "namespace",
								    "objectId",
								    0,
								    "");

	assertTrue(lsid0 != lsid1);
	assertTrue(lsid0.equals(lsid1));
	assertTrue(lsid0.hashCode() == lsid1.hashCode());
	assertTrue(lsid1 != lsid0);
	assertTrue(lsid1.equals(lsid0));
	assertTrue(lsid1.hashCode() == lsid0.hashCode());

	assertTrue(lsid0.equals(lsid2) == false);
	assertTrue(lsid0.hashCode() != lsid2.hashCode());
	assertTrue(lsid2.equals(lsid0) == false);
	assertTrue(lsid2.hashCode() != lsid0.hashCode());

	assertTrue(lsid0.equals(lsid3) == false);
	assertTrue(lsid0.hashCode() != lsid3.hashCode());
	assertTrue(lsid3.equals(lsid0) == false);
	assertTrue(lsid3.hashCode() != lsid0.hashCode());

	assertTrue(lsid0.equals(lsid4) == false);
	assertTrue(lsid0.hashCode() != lsid4.hashCode());
	assertTrue(lsid4.equals(lsid0) == false);
	assertTrue(lsid4.hashCode() != lsid0.hashCode());
	assertTrue(lsid0.equals(lsid5) == false);
	assertTrue(lsid0.hashCode() != lsid5.hashCode());
	assertTrue(lsid5.equals(lsid0) == false);
	assertTrue(lsid5.hashCode() != lsid0.hashCode());
	assertTrue(lsid4.equals(lsid5) == false);
	assertTrue(lsid4.hashCode() != lsid5.hashCode());
	assertTrue(lsid5.equals(lsid4) == false);
	assertTrue(lsid5.hashCode() != lsid4.hashCode());

	assertTrue("authority:namespace:objectId:0:security".equals(lsid0.toString()));
	assertTrue("authority:namespace:objectId:0:security".equals(lsid1.toString()));
	assertTrue("authority:namespace:objectId:1:security".equals(lsid2.toString()));
	assertTrue("authority1:namespace:objectId:0:security".equals(lsid3.toString()));
	assertTrue("authority:namespace:objectId:0".equals(lsid4.toString()));
	assertTrue("authority:namespace:objectId:0:".equals(lsid5.toString()));
    }
    public void testSerialization()
    {
	LifeScienceIdentifier lsid0 = LifeScienceIdentifier.valueOf("authority",
								    "namespace",
								    "objectId",
								    0,
								    "security");
	LifeScienceIdentifier lsid1 = null;

	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	try 
	{
	    File f = File.createTempFile("lsid", ".ser");
	    oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
	    oos.writeObject(lsid0);
	    oos.flush();

	    ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
	    lsid1 = (LifeScienceIdentifier) ois.readObject();

	    assertTrue(lsid0 != lsid1);
	    assertTrue(lsid0.equals(lsid1) == true);
	}
	catch (Exception e)
	{
	    fail(e.getMessage());
	}
	finally
	{
	    try
	    {
		oos.close();
	    }
	    catch (Exception e1) {}
	    try
	    {
		ois.close();
	    }
	    catch (Exception e2) {}
	}
    }
}
