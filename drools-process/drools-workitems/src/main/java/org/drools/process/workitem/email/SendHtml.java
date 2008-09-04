package org.drools.process.workitem.email;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

public class SendHtml {

    public static void sendHtml(Email email) {
        org.drools.process.workitem.email.Message message = email.getMessage();
        Connection connection = email.getConnection();

        String subject = message.getSubject();
        String from = message.getFrom();
        String mailhost = connection.getHost();
        String mailer = "sendhtml";
        boolean debug = false;
        try {
            Properties props = System.getProperties();
            // XXX - could use Session.getTransport() and Transport.connect()
            // XXX - assume we're using SMTP
            if ( mailhost != null ) props.put( "mail.smtp.host", mailhost );
            // Get a Session object
            Session session = Session.getInstance( props, null );
            if ( debug ) session.setDebug( true );
            // construct the message
            Message msg = new MimeMessage( session );
            if ( from != null ) {
            	msg.setFrom( new InternetAddress( from ) );
            } else {
            	msg.setFrom();
            }
            for ( Recipient recipient : message.getRecipients().getRecipients() ) {
                RecipientType type = null;
                if ( "to".equals( recipient.getType() ) ) {
                    type = Message.RecipientType.TO;
                } else if ( "cc".equals( recipient.getType() ) ) {
                    type = Message.RecipientType.CC;
                } else if ( "bcc".equals( recipient.getType() ) ) {
                    type = Message.RecipientType.BCC;
                }

                msg.addRecipients( type, InternetAddress.parse( recipient.getEmail(), false ) );
            }
            msg.setSubject( subject );
            collect( message.getBody(), msg );
            msg.setHeader( "X-Mailer", mailer );
            msg.setSentDate( new Date() );
            // send the thing off
            Transport.send( msg );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static void collect(String body, Message msg) throws MessagingException, IOException {
        String subject = msg.getSubject();
        StringBuffer sb = new StringBuffer();
        sb.append( "<HTML>\n" );
        sb.append( "<HEAD>\n" );
        sb.append( "<TITLE>\n" );
        sb.append( subject + "\n" );
        sb.append( "</TITLE>\n" );
        sb.append( "</HEAD>\n" );
        sb.append( "<BODY>\n" );
        sb.append( "<H1>" + subject + "</H1>" + "\n" );
        sb.append( body );
        sb.append( "</BODY>\n" );
        sb.append( "</HTML>\n" );
        msg.setDataHandler( new DataHandler( new ByteArrayDataSource( sb.toString(), "text/html" ) ) );
    }
}