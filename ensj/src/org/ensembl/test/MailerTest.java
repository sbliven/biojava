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

package org.ensembl.test;

import java.util.Date;

import junit.framework.TestCase;

import org.ensembl.util.Mailer;

/**
 * Unit tests for Mailer. WARNING: this test is designed to run inside the
 * ebi for users whose user.name is the same as there email address. It will
 * fail for other users.
 */
public class MailerTest extends TestCase {

  public MailerTest(String testName) {

    super(testName);

  }

  public void testNormal() {

    Mailer mailer = new Mailer("mailserv.ebi.ac.uk");

    // guess email address from login name - will only work if email address is the same as the user's first name, and email can
    // be sent without a domain part.
    String name = System.getProperty("user.name").toLowerCase();
    int end = name.indexOf(" ");
    if (end > -1)
      name = name.substring(0, end);

    String className = this.getClass().getName();
    String[] recipients = { name };
    mailer.sendMail(
      recipients,
      "Result of " + className,
      "This email was sent from the 'testNormal()' method of "
        + className
        + " unit test on "
        + new Date().toString(),
      "JUnit test <a@null.com>");

  }

}