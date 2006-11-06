/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.test;

import junit.framework.TestCase;

import org.ensembl.util.NamedTimer;

/**
 * Unit tests for NamedTimer
 */
public class NamedTimerTest extends TestCase {

    public NamedTimerTest(String testName) {

        super(testName);

    }

    public void testNormal() {

        NamedTimer nt = new NamedTimer();
        nt.start("one");
        sleep(500);
        long duration = nt.stop("one");
        assertTrue(duration > 450);
  
        long d2 = nt.getDuration("one");
        assertEquals(duration, d2);
        
        nt.start("short");
        sleep(100);
        String f = nt.format(nt.stop("short"));
        assertEquals(f, "0.1s");
        
    }
    
   //---------------------------------------------------------------------
    
    public void testDuplicate() {
        
        try {
            
            NamedTimer nt = new NamedTimer();
            nt.start("two");
            nt.start("two");
            fail("RuntimeException should have been thrown for duplicate timers");
            
        } catch (RuntimeException e) {

        }
        
    }
    
    //---------------------------------------------------------------------
    
    public void testNotStarted() {
        
        try {
            
            NamedTimer nt = new NamedTimer();
            long duration = nt.stop("three");
            fail("RuntimeException should have been thrown for non-started timer");
            
        } catch (RuntimeException e) {

        }
        
    }
    
    //---------------------------------------------------------------------
    
    public void testFormat() {
        
        NamedTimer nt = new NamedTimer();
        nt.start("twosec");
        nt.start("onemintwosec");
        sleep(2010);
        String f = nt.format(nt.stop("twosec"));
        //System.out.println(f);
        assertEquals(f, "2s");
        
        // commented out because slows down testing when run
        // all tests
//        sleep(60010);
//        f = nt.format(nt.stop("onemintwosec"));
//        System.out.println(f);
//        assertEquals(f, "1m 2s");
        
    }
    
    //---------------------------------------------------------------------
    

    private void sleep(long ms) {

        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

    }

}