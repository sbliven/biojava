/*
 * Copyright (C) 2002 EBI, GRL
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

package org.ensembl.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Simple class to allow SMTP email to be sent.
 */
public class Mailer {

    private String smtpServer;

    public Mailer(String smtpServer) {

        this.smtpServer = smtpServer;

    }

    public void sendMail(String recipients[], String subject, String message, String from) {

        // Set the host SMTP address
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpServer);

        // create some properties and get the default Session
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(false);

        // create a message
        Message msg = new MimeMessage(session);

        try {

            // set the from and to address
            InternetAddress addressFrom = new InternetAddress(from);
            msg.setFrom(addressFrom);

            InternetAddress[] addressTo = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addressTo[i] = new InternetAddress(recipients[i]);
            }
            msg.setRecipients(Message.RecipientType.TO, addressTo);

            // Setting the Subject and ContentType
            msg.setSubject(subject);
            msg.setContent(message, "text/plain");
            Transport.send(msg);

        } catch (MessagingException me) {

            me.printStackTrace();

        }

    }
    
    // -------------------------------------------------------------------------
    
    public void sendMail(String recipient, String subject, String message, String from) {

        String[] recipients = { recipient };
        sendMail(recipients, subject, message, from);
        
    }
    
    // -------------------------------------------------------------------------
    

}