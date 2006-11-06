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

package org.ensembl.test;

import java.util.List;

import junit.framework.TestCase;

import org.ensembl.idmapping.ScoredMappingMatrix;

/**
 * Unit tests for ScoredMappingMatrix
 */
public class ScoredMappingMatrixTest extends TestCase {

    private ScoredMappingMatrix smm;

    public ScoredMappingMatrixTest(String testName) {

        super(testName);

        smm = new ScoredMappingMatrix();

        // test matrix, source IDs start from 1, target start from 1000
        for (int i = 0; i < 10; i++) {
            smm.addScore(i, i + 1000, (float) Math.random());
        }

        // some more, different ordering to test sorting in trees
        for (int i = 50; i < 55; i++) {
            smm.addScore(i, 1200 - i, (float) Math.random());
        }

        // some sources with more than one target
        for (int i = 0; i < 5; i++) {
            smm.addScore(70, i + 1200, (float) Math.random());
        }
        // some targets with more than one source
        for (int i = 0; i < 5; i++) {
            smm.addScore(i, 1300, (float) Math.random());
        }

        //smm.dump();

    }

    public void testHasScore() {

        assertTrue(smm.hasScore(1, 1001));
        assertTrue(!smm.hasScore(1, 10));

    }

    public void testGetScore() {

        assertTrue(smm.getScore(1, 1001) > 0.0f);

    }

    public void testSourceEntries() {
    
        List s = smm.sourceEntries(70);
        assertNotNull(s);
        assertEquals(s.size(), 5);
    }

    public void testTargetEntries() {

        List t = smm.targetEntries(1300);
        assertNotNull(t);
        assertEquals(t.size(), 5);

    }

    public void testRemove() {

        ScoredMappingMatrix smm = new ScoredMappingMatrix();
        smm.addScore(1, 2, 1.0f);
        smm.addScore(2, 3, 1.0f);
        assertTrue(smm.getScore(1, 2) > 0.0f);
        assertTrue(smm.getScore(2, 3) > 0.0f);
        int originalSourceLength = smm.getAllSources().length;
        int originalTargetLength = smm.getAllTargets().length;
        smm.remove(1,2);
        assertEquals(smm.getScore(1, 2), 0.0f, 0.000001f);
        int newSourceLength = smm.getAllSources().length;
        int newTargetLength = smm.getAllTargets().length;
        assertEquals(newSourceLength, originalSourceLength-1);
        assertEquals(newTargetLength, originalTargetLength-1);

    }

    public void testCombineWith() {

        ScoredMappingMatrix smm1 = new ScoredMappingMatrix();
        smm1.addScore(1, 2, 0.5f);
        smm1.addScore(2, 3, 0.5f);

        ScoredMappingMatrix smm2 = new ScoredMappingMatrix();
        smm2.addScore(5, 6, 1.0f);
        smm2.addScore(1, 2, 0.1f);
        smm2.addScore(2, 3, 1.0f);

        smm1.combineWith(smm2);

        // combined one should have union of both
        assertTrue(smm1.getScore(5, 6) > 0.0f);

        // "overlapping" entries should use highest score
        assertTrue(smm1.getScore(2, 3) > 0.9f);
        assertTrue(smm1.getScore(2, 3) > 0.4f);

    }
}