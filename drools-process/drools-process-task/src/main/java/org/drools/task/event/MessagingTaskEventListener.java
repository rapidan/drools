package org.drools.task.event;

import java.util.Iterator;
import java.util.List;

import org.drools.eventmessaging.EventKey;
import org.drools.eventmessaging.EventKeys;
import org.drools.eventmessaging.EventTriggerTransport;
import org.drools.eventmessaging.Payload;

public class MessagingTaskEventListener implements TaskEventListener {
	
    private EventKeys keys;
    
    public MessagingTaskEventListener(EventKeys keys) {
        this.keys = keys;
    }
    
    public void taskClaimed(TaskClaimedEvent event) {        
        EventKey key = new TaskEventKey(TaskClaimedEvent.class, event.getTaskId() );
        List<EventTriggerTransport> targets = keys.getTargets( key );
        if ( targets == null ){
            return;
        }
        Payload payload = new EventPayload( event );
        for ( Iterator<EventTriggerTransport> it = targets.iterator(); it.hasNext(); ) {
            EventTriggerTransport target = it.next();
            target.trigger( payload );
            if ( target.isRemove() ) {
                it.remove();
            }
        }
        if ( targets.isEmpty() ) {
            keys.removeKey( key );
        }
    }

    public void taskCompleted(TaskCompletedEvent event) {
    	System.out.println("MessagingTaskEventListener.taskCompleted " + event.getTaskId());
        EventKey key = new TaskEventKey(TaskCompletedEvent.class, event.getTaskId() );
        List<EventTriggerTransport> targets = keys.getTargets( key );
        if ( targets == null ){
        	key = new TaskEventKey(TaskCompletedEvent.class, -1);
        	targets = keys.getTargets( key );
        	if (targets == null) {
            	System.out.println("Found no listeners"); 
        		return;
        	} else {
            	System.out.println("Found generic listeners: " + targets.size()); 
        	}
        } else {
        	System.out.println("Found specific listeners: " + targets.size()); 
        	key = new TaskEventKey(TaskCompletedEvent.class, -1);
        	List<EventTriggerTransport> additionalTargets = keys.getTargets( key );
        	if (additionalTargets != null) {
            	System.out.println("Found generic listeners: " + additionalTargets.size()); 
        		targets.addAll(additionalTargets);
        	}
        }
        Payload payload = new EventPayload( event );
        for ( Iterator<EventTriggerTransport> it = targets.iterator(); it.hasNext(); ) {
            EventTriggerTransport target = it.next();
            target.trigger( payload );
            if ( target.isRemove() ) {
                it.remove();
            }
        }
        if ( targets.isEmpty() ) {
            keys.removeKey( key );
        }   
    }

	public void taskFailed(TaskFailedEvent event) {
        EventKey key = new TaskEventKey(TaskFailedEvent.class, event.getTaskId() );
        List<EventTriggerTransport> targets = keys.getTargets( key );
        if ( targets == null ){
        	key = new TaskEventKey(TaskFailedEvent.class, -1);
        	targets = keys.getTargets( key );
        	if (targets == null) {
        		return;
        	}
        } else {
        	key = new TaskEventKey(TaskFailedEvent.class, -1);
        	List<EventTriggerTransport> additionalTargets = keys.getTargets( key );
        	if (additionalTargets != null) {
        		targets.addAll(additionalTargets);
        	}
        }
        Payload payload = new EventPayload( event );
        for ( Iterator<EventTriggerTransport> it = targets.iterator(); it.hasNext(); ) {
            EventTriggerTransport target = it.next();
            target.trigger( payload );
            if ( target.isRemove() ) {
                it.remove();
            }
        }
        if ( targets.isEmpty() ) {
            keys.removeKey( key );
        }
	}

	public void taskSkipped(TaskSkippedEvent event) {
        EventKey key = new TaskEventKey(TaskSkippedEvent.class, event.getTaskId() );
        List<EventTriggerTransport> targets = keys.getTargets( key );
        if ( targets == null ){
        	key = new TaskEventKey(TaskSkippedEvent.class, -1);
        	targets = keys.getTargets( key );
        	if (targets == null) {
        		return;
        	}
        } else {
        	key = new TaskEventKey(TaskSkippedEvent.class, -1);
        	List<EventTriggerTransport> additionalTargets = keys.getTargets( key );
        	if (additionalTargets != null) {
        		targets.addAll(additionalTargets);
        	}
        }
        Payload payload = new EventPayload( event );
        for ( Iterator<EventTriggerTransport> it = targets.iterator(); it.hasNext(); ) {
            EventTriggerTransport target = it.next();
            target.trigger( payload );
            if ( target.isRemove() ) {
                it.remove();
            }
        }
        if ( targets.isEmpty() ) {
            keys.removeKey( key );
        }
	}

}
