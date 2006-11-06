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

package org.ensembl.variation.test;

import java.util.List;
import java.util.logging.Logger;

import org.ensembl.variation.datamodel.Individual;
import org.ensembl.variation.datamodel.Population;

/**
 * Tests the implementation of the Individual support.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class IndividualTest extends VariationBase {

  private static final Logger logger = Logger.getLogger(IndividualTest.class
      .getName());
  
  
  public IndividualTest(String name) throws Exception {
    super(name);
  }

  public void testFetchByID() throws Exception {
    final long ID = 980;
    Individual i = vdriver.getIndividualAdaptor().fetch(ID);
    assertEquals(ID, i.getInternalID());
    check(i);
  }

  public void testFetchByName() throws Exception {
    String name = "CEPH104.03";
    List is = vdriver.getIndividualAdaptor().fetch(name);
    check(is);

  }

  public void testFetchByPopulation() throws Exception {
    // useful to pick a population that doesn't have too many individuals
    // to speed up test. Useful SQL:
    // select count(*) as c, population_sample_id from individual_population group by
    // population_sample_id order by c;

    final long pID = 12;
    Population p = vdriver.getPopulationAdaptor().fetch(pID);
    assertNotNull(p);
    List is = vdriver.getIndividualAdaptor().fetch(p);
    check(is);
    for (int i = 0, n = is.size(); i < n; i++) {
      Individual individual = (Individual) is.get(i);
      List populations = individual.getPopulations();
      assertTrue(populations.size() > 0);
      boolean matchingPopFound = false;
      for (int j = 0; !matchingPopFound && j < populations.size(); j++) {
        Population population = (Population) populations.get(j);
        matchingPopFound = population.getInternalID() == pID;
      }
      assertTrue(matchingPopFound);
    }
  }

  public void testFetchByParent() throws Exception {
    Individual parent = vdriver.getIndividualAdaptor().fetch(1001);
    assertNotNull(parent);
    List is = vdriver.getIndividualAdaptor().fetch(parent);
    check(is);
    for (int i = 0, n = is.size(); i < n; i++) {
      Individual individual = (Individual) is.get(i);
      assertTrue(individual.getMother().getInternalID() == parent
          .getInternalID()
          || individual.getFather().getInternalID() == parent.getInternalID());
    }
  }

  /**
   * Checks that all the attributes in all the Individual are set.
   * 
   * @param individual
   *          individual to check
   */
  private void check(List individuals) {
    assertTrue(individuals.size() > 0);
    for (int i = 0, n = individuals.size(); i < n; i++)
      check((Individual) individuals.get(i));

  }

  /**
   * Checks that all the attributes are available on the individual and that
   * associated items make sense.
   * 
   * @param individual
   *          individual to check
   */
  private void check(Individual individual) {

    assertTrue(individual.getInternalID() > 0);
    assertNotNull(individual.getName());
    assertNotNull(individual.getDescription());
    assertTrue(individual.getPopulations().size() > 0);

    if (individual.getFather() != null) {
      assertTrue(individual.getFather().getInternalID() > 0);
      String fgender = individual.getFather().getGender();
      assertTrue(fgender == null || fgender.equals("Male"));
    }

    if (individual.getMother() != null) {
      assertTrue(individual.getMother().getInternalID() > 0);
      String mgender = individual.getMother().getGender();
      assertTrue(mgender == null || mgender.equals("Female"));
    }
  }

}