package org.drools.process.workitem.wsht;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.drools.eventmessaging.EventKey;
import org.drools.eventmessaging.EventResponseHandler;
import org.drools.eventmessaging.Payload;
import org.drools.process.instance.WorkItem;
import org.drools.process.instance.WorkItemHandler;
import org.drools.process.instance.WorkItemManager;
import org.drools.task.I18NText;
import org.drools.task.OrganizationalEntity;
import org.drools.task.PeopleAssignments;
import org.drools.task.Task;
import org.drools.task.TaskData;
import org.drools.task.User;
import org.drools.task.event.TaskCompletedEvent;
import org.drools.task.event.TaskEvent;
import org.drools.task.event.TaskEventKey;
import org.drools.task.event.TaskFailedEvent;
import org.drools.task.event.TaskSkippedEvent;
import org.drools.task.service.MinaTaskClient;
import org.drools.task.service.TaskClientHandler;
import org.drools.task.service.TaskClientHandler.AddTaskResponseHandler;

public class WSHumanTaskHandler implements WorkItemHandler {

	private String ipAddress = "127.0.0.1";
	private int port = 9123;
	private MinaTaskClient client;
	private Map<Long, WorkItemManager> managers = new HashMap<Long, WorkItemManager>();
	private Map<Long, Long> idMapping = new HashMap<Long, Long>();

	public void setConnection(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public void connect() {
		if (client == null) {
			client = new MinaTaskClient(
				"org.drools.process.workitem.wsht.WSHumanTaskHandler", new TaskClientHandler());
			NioSocketConnector connector = new NioSocketConnector();
			SocketAddress address = new InetSocketAddress(ipAddress, port);
			boolean connected = client.connect(connector, address);
			if (!connected) {
				throw new IllegalArgumentException(
					"Could not connect task client");
			}
		}
	}

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		connect();
		Task task = new Task();
		String taskName = (String) workItem.getParameter("TaskName");
		if (taskName != null) {
			List<I18NText> names = new ArrayList<I18NText>();
			names.add(new I18NText("en-UK", taskName));
			task.setNames(names);
		}
		String comment = (String) workItem.getParameter("Comment");
		if (comment != null) {
			List<I18NText> descriptions = new ArrayList<I18NText>();
			descriptions.add(new I18NText("en-UK", comment));
			task.setDescriptions(descriptions);
			List<I18NText> subjects = new ArrayList<I18NText>();
			subjects.add(new I18NText("en-UK", comment));
			task.setSubjects(subjects);
		}
		String priority = (String) workItem.getParameter("Priority");
		if (priority != null) {
			try {
				task.setPriority(new Integer(priority));
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		TaskData taskData = new TaskData();
		taskData.setWorkItemId(workItem.getId());
		taskData.setSkipable(!"false".equals(workItem.getParameter("Skippable")));
		task.setTaskData(taskData);
		String actorId = (String) workItem.getParameter("ActorId");
		if (actorId != null) {
			PeopleAssignments assignments = new PeopleAssignments();
			List<OrganizationalEntity> potentialOwners = new ArrayList<OrganizationalEntity>();
			String[] actorIds = actorId.split(",");
			for (String id: actorIds) {
				User user = new User();
				user.setId(id.trim());
				potentialOwners.add(user);
			}
			assignments.setPotentialOwners(potentialOwners);
			List<OrganizationalEntity> businessAdministrators = new ArrayList<OrganizationalEntity>();
			businessAdministrators.add(new User("Administrator"));
			assignments.setBusinessAdministrators(businessAdministrators);
			task.setPeopleAssignments(assignments);
		}
		
		TaskWorkItemAddTaskResponseHandler taskResponseHandler =
			new TaskWorkItemAddTaskResponseHandler(this.client, this.managers,
				this.idMapping, manager, workItem.getId());
		client.addTask(task, taskResponseHandler);
	}
	
	public void dispose() {
		if (client != null) {
			client.disconnect();
		}
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		Long taskId = idMapping.get(workItem.getId());
		if (taskId != null) {
			synchronized (idMapping) {
				idMapping.remove(taskId);
			}
			synchronized (managers) {
				managers.remove(taskId);
			}
			client.skip(taskId, "Administrator", null);
		}
	}
	
    public static class TaskWorkItemAddTaskResponseHandler implements AddTaskResponseHandler {
        private volatile String error;
        private Map<Long, WorkItemManager> managers;
        private Map<Long, Long> idMapping;
        private WorkItemManager manager;
        private long workItemId;
        private MinaTaskClient client;
        
        public TaskWorkItemAddTaskResponseHandler(MinaTaskClient client,
        		Map<Long, WorkItemManager> managers,  Map<Long, Long> idMapping,
        		WorkItemManager manager, long workItemId) {
            this.client = client;
            this.managers = managers;
            this.idMapping = idMapping;
            this.manager = manager;
            this.workItemId = workItemId;
        }
        
        public void execute(long taskId) {
        	synchronized ( managers ) {
                managers.put(taskId, this.manager);           
            }
            synchronized ( idMapping ) {
                idMapping.put(workItemId, taskId);           
            }
            System.out.println("Created task " + taskId + " for work item " + workItemId);
            
            EventKey key = new TaskEventKey(TaskCompletedEvent.class, taskId );           
            TaskCompletedHandler eventResponseHandler = new TaskCompletedHandler(workItemId, taskId, managers); 
            client.registerForEvent( key, true, eventResponseHandler );
            key = new TaskEventKey(TaskFailedEvent.class, taskId );           
            client.registerForEvent( key, true, eventResponseHandler );
            key = new TaskEventKey(TaskSkippedEvent.class, taskId );           
            client.registerForEvent( key, true, eventResponseHandler );
        }

        public void setError(String error) {
            this.error = error;         
        }
        
        public String getError() {
            return this.error;
        }       
    }
    
    private static class TaskCompletedHandler implements EventResponseHandler {
        private volatile String error;
        
        private long workItemId;
        private long taskId;
        private Map<Long, WorkItemManager> managers;
        
        public TaskCompletedHandler(long workItemId, long taskId, Map<Long, WorkItemManager> managers) {
            this.workItemId = workItemId;
            this.taskId = taskId;
            this.managers = managers;
        }

        public void execute(Payload payload) {
            TaskEvent event = ( TaskEvent ) payload.get();
        	if ( event.getTaskId() != taskId ) {
                // defensive check that should never happen, just here for testing                
                this.error = "Expected task id and arrived task id do not march";
                return;
            }
        	if (event instanceof TaskCompletedEvent) {
		        synchronized ( this.managers ) {
		            WorkItemManager manager = this.managers.get(taskId);
		            if (manager != null) {
		            	manager.completeWorkItem(workItemId, null);   
		            }
		        }
        	} else {
        		synchronized ( this.managers ) {
        			WorkItemManager manager = this.managers.get(taskId);
		            if (manager != null) {
		            	manager.abortWorkItem(workItemId);
		            }
		        }
        	}
        }

        public void setError(String error) {
            this.error = error;
        }
        
        public String getError() {
            return this.error;
        }
    }

}