package org.drools.process.workitem.email;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.drools.process.instance.WorkItemManager;
import org.drools.process.instance.impl.WorkItemImpl;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;


import junit.framework.TestCase;

public class EmailWorkItemHandlerTest extends TestCase {
    Wiser wiser;
    @Override
    protected void setUp() throws Exception {
         wiser = new Wiser();
         wiser.start();

    }
    
    @Override
    protected void tearDown() throws Exception {
        wiser.stop();
    }
    
    public void testSingleTo() throws Exception {
        EmailWorkItemHandler handler = new EmailWorkItemHandler();
        handler.setConnection( "localhost", "25", null, null );   
        
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter( "To", "person1@domain.com" );
        workItem.setParameter( "From", "person2@domain.com" );
        workItem.setParameter( "Reply-To", "person3@domain.com" );
        workItem.setParameter( "Subject", "Subject 1" );
        workItem.setParameter( "Body", "Body 1" );
        
        WorkItemManager manager = new WorkItemManager();
        handler.executeWorkItem( workItem, manager );
        
        assertEquals( 1, wiser.getMessages().size() );
        
        MimeMessage msg = (( WiserMessage  ) wiser.getMessages().get( 0 )).getMimeMessage();
        assertEquals( workItem.getParameter( "Body" ), msg.getContent() );
        assertEquals( workItem.getParameter( "Subject" ), msg.getSubject() );
        assertEquals( workItem.getParameter( "From" ), ((InternetAddress)msg.getFrom()[0]).getAddress() );
        assertEquals( workItem.getParameter( "Reply-To" ), ((InternetAddress)msg.getReplyTo()[0]).getAddress() );
        assertEquals( workItem.getParameter( "To" ), ((InternetAddress)msg.getRecipients( RecipientType.TO )[0]).getAddress() );
        assertNull( msg.getRecipients( RecipientType.CC ) );
        assertNull( msg.getRecipients( RecipientType.BCC ) );
    }
    
    public void testSingleToWithSingleCCAndBCC() throws Exception {
        EmailWorkItemHandler handler = new EmailWorkItemHandler();
        handler.setConnection( "localhost", "25", null, null );   
        
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter( "To", "person1@domain.com" );
        workItem.setParameter( "Cc", "person2@domain.com" );
        workItem.setParameter( "Bcc", "person3@domain.com" );
        workItem.setParameter( "From", "person4@domain.com" );
        workItem.setParameter( "Reply-To", "person5@domain.com" );
        workItem.setParameter( "Subject", "Subject 1" );
        workItem.setParameter( "Body", "Body 1" );
        
        WorkItemManager manager = new WorkItemManager();
        handler.executeWorkItem( workItem, manager );
        
        assertEquals( 3, wiser.getMessages().size() );
        
        List<String> list = new ArrayList<String>(3);
        list.add( wiser.getMessages().get( 0 ).getEnvelopeReceiver() );
        list.add( wiser.getMessages().get( 1 ).getEnvelopeReceiver() );
        list.add( wiser.getMessages().get( 2 ).getEnvelopeReceiver() );
        
        assertTrue( list.contains("person1@domain.com"));
        assertTrue( list.contains("person2@domain.com"));
        assertTrue( list.contains("person3@domain.com"));
        
        
        MimeMessage msg = (( WiserMessage  ) wiser.getMessages().get( 0 )).getMimeMessage();
        assertEquals( workItem.getParameter( "From" ), wiser.getMessages().get( 0 ).getEnvelopeSender() );
        assertEquals( workItem.getParameter( "Body" ), msg.getContent() );
        assertEquals( workItem.getParameter( "Subject" ), msg.getSubject() );
        assertEquals( workItem.getParameter( "From" ), ((InternetAddress)msg.getFrom()[0]).getAddress() );
        assertEquals( workItem.getParameter( "Reply-To" ), ((InternetAddress)msg.getReplyTo()[0]).getAddress() );
        assertEquals( workItem.getParameter( "To" ), ((InternetAddress)msg.getRecipients( RecipientType.TO )[0]).getAddress() );
        assertEquals( workItem.getParameter( "Cc" ),((InternetAddress)msg.getRecipients( RecipientType.CC )[0]).getAddress()  );
        
        msg = (( WiserMessage  ) wiser.getMessages().get( 1 )).getMimeMessage();
        assertEquals( workItem.getParameter( "From" ), wiser.getMessages().get( 1 ).getEnvelopeSender() );
        assertEquals( workItem.getParameter( "Body" ), msg.getContent() );
        assertEquals( workItem.getParameter( "Subject" ), msg.getSubject() );
        assertEquals( workItem.getParameter( "From" ), ((InternetAddress)msg.getFrom()[0]).getAddress() );
        assertEquals( workItem.getParameter( "Reply-To" ), ((InternetAddress)msg.getReplyTo()[0]).getAddress() );
        assertEquals( workItem.getParameter( "To" ), ((InternetAddress)msg.getRecipients( RecipientType.TO )[0]).getAddress() );
        assertEquals( workItem.getParameter( "Cc" ),((InternetAddress)msg.getRecipients( RecipientType.CC )[0]).getAddress()  );
        
        msg = (( WiserMessage  ) wiser.getMessages().get( 2 )).getMimeMessage();
        assertEquals( workItem.getParameter( "From" ), wiser.getMessages().get( 2 ).getEnvelopeSender() );
        assertEquals( workItem.getParameter( "Body" ), msg.getContent() );
        assertEquals( workItem.getParameter( "Subject" ), msg.getSubject() );
        assertEquals( workItem.getParameter( "From" ), ((InternetAddress)msg.getFrom()[0]).getAddress() );
        assertEquals( workItem.getParameter( "Reply-To" ), ((InternetAddress)msg.getReplyTo()[0]).getAddress() );
        assertEquals( workItem.getParameter( "To" ), ((InternetAddress)msg.getRecipients( RecipientType.TO )[0]).getAddress() );
        assertEquals( workItem.getParameter( "Cc" ),((InternetAddress)msg.getRecipients( RecipientType.CC )[0]).getAddress()  );        
    }    
    
    public void testMultipleToWithSingleCCAndBCC() throws Exception {
        EmailWorkItemHandler handler = new EmailWorkItemHandler();
        handler.setConnection( "localhost", "25", null, null );   
        
        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter( "To", "person1@domain.com; person2@domain.com" );
        workItem.setParameter( "Cc", "person3@domain.com; person4@domain.com"  );
        workItem.setParameter( "Bcc","person5@domain.com; person6@domain.com"  );
        workItem.setParameter( "From", "person4@domain.com" );
        workItem.setParameter( "Reply-To", "person5@domain.com" );
        workItem.setParameter( "Subject", "Subject 1" );
        workItem.setParameter( "Body", "Body 1" );
        
        WorkItemManager manager = new WorkItemManager();
        handler.executeWorkItem( workItem, manager );
        
        assertEquals( 6, wiser.getMessages().size() );
        
        List<String> list = new ArrayList<String>(6);
        list.add( wiser.getMessages().get( 0 ).getEnvelopeReceiver() );
        list.add( wiser.getMessages().get( 1 ).getEnvelopeReceiver() );
        list.add( wiser.getMessages().get( 2 ).getEnvelopeReceiver() );
        list.add( wiser.getMessages().get( 3 ).getEnvelopeReceiver() );
        list.add( wiser.getMessages().get( 4 ).getEnvelopeReceiver() );
        list.add( wiser.getMessages().get( 5 ).getEnvelopeReceiver() );
        
        assertTrue( list.contains("person1@domain.com"));
        assertTrue( list.contains("person2@domain.com"));
        assertTrue( list.contains("person3@domain.com"));
        assertTrue( list.contains("person4@domain.com"));
        assertTrue( list.contains("person5@domain.com"));
        assertTrue( list.contains("person6@domain.com"));
                
        // We know from previous test that all MimeMessages will be identical
        MimeMessage msg = (( WiserMessage  ) wiser.getMessages().get( 0 )).getMimeMessage();
        assertEquals( workItem.getParameter( "From" ), wiser.getMessages().get( 0 ).getEnvelopeSender() );
        assertEquals( workItem.getParameter( "Body" ), msg.getContent() );
        assertEquals( workItem.getParameter( "Subject" ), msg.getSubject() );
        assertEquals( workItem.getParameter( "From" ), ((InternetAddress)msg.getFrom()[0]).getAddress() );
        assertEquals( workItem.getParameter( "Reply-To" ), ((InternetAddress)msg.getReplyTo()[0]).getAddress() );
        assertEquals( workItem.getParameter( "To" ), ((InternetAddress)msg.getRecipients( RecipientType.TO )[0]).getAddress() + "; " + ((InternetAddress)msg.getRecipients( RecipientType.TO )[1]).getAddress() );
        assertEquals( workItem.getParameter( "Cc" ),((InternetAddress)msg.getRecipients( RecipientType.CC )[0]).getAddress()  + "; " +  ((InternetAddress)msg.getRecipients( RecipientType.CC )[1]).getAddress() );       
    }    
}