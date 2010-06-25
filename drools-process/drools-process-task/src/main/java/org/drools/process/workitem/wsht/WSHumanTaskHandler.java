package org.drools.process.workitem.wsht;

import org.drools.SystemEventListenerFactory;
import org.drools.eventmessaging.EventResponseHandler;
import org.drools.eventmessaging.Payload;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.drools.task.*;
import org.drools.task.event.*;
import org.drools.task.service.ContentData;
import org.drools.task.service.TaskClient;
import org.drools.task.service.mina.MinaTaskClientConnector;
import org.drools.task.service.mina.MinaTaskClientHandler;
import org.drools.task.service.responsehandlers.AbstractBaseResponseHandler;
import org.drools.task.service.TaskClientHandler.GetContentResponseHandler;
import org.drools.task.service.TaskClientHandler.GetTaskResponseHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WSHumanTaskHandler implements WorkItemHandler {

	private String ipAddress = "127.0.0.1";
	private int port = 9123;
	private TaskClient client;
	private WorkItemManager manager = null;

	public void setConnection(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public void setClient(TaskClient client) {
		this.client = client;
	}
	
	public void connect() {
		if (client == null) {
			client = new TaskClient(new MinaTaskClientConnector("org.drools.process.workitem.wsht.WSHumanTaskHandler",
										new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
			
			boolean connected = client.connect(ipAddress, port);
			if (!connected) {
				throw new IllegalArgumentException("Could not connect task client");
			}
		}
		TaskEventKey key = new TaskEventKey(TaskCompletedEvent.class, -1);           
		TaskCompletedHandler eventResponseHandler = new TaskCompletedHandler(manager, client);
		client.registerForEvent(key, false, eventResponseHandler);
		key = new TaskEventKey(TaskFailedEvent.class, -1);           
		client.registerForEvent(key, false, eventResponseHandler);
		key = new TaskEventKey(TaskSkippedEvent.class, -1);           
		client.registerForEvent(key, false, eventResponseHandler);
		System.out.println("Registered human task listener");
	}
	
	public void setManager(WorkItemManager manager) {
		this.manager = manager;
	}

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		if (this.manager == null) {
			this.manager = manager;
		} else {
			if (this.manager != manager) {
				throw new IllegalArgumentException(
					"This WSHumanTaskHandler can only be used for one WorkItemManager");
			}
		}
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
		String priorityString = (String) workItem.getParameter("Priority");
		int priority = 0;
		if (priorityString != null) {
			try {
				priority = new Integer(priorityString);
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		task.setPriority(priority);
		
		TaskData taskData = new TaskData();
		taskData.setWorkItemId(workItem.getId());
		taskData.setSkipable(!"false".equals(workItem.getParameter("Skippable")));
        //Sub Task Data
        Long parentId = (Long) workItem.getParameter("ParentId");
        if(parentId != null){
            taskData.setParentId(parentId);
        }

        String subTaskStrategiesCommaSeparated = (String) workItem.getParameter("SubTaskStrategies");
        if(subTaskStrategiesCommaSeparated!= null && !subTaskStrategiesCommaSeparated.equals("")){
            String[] subTaskStrategies =  subTaskStrategiesCommaSeparated.split(",");
            List<SubTasksStrategy> strategies = new ArrayList<SubTasksStrategy>();
            for(String subTaskStrategyString : subTaskStrategies){
                SubTasksStrategy subTaskStrategy = SubTasksStrategyFactory.newStrategy(subTaskStrategyString);
                strategies.add(subTaskStrategy);
            }
            task.setSubTaskStrategies(strategies);
        }

        PeopleAssignments assignments = new PeopleAssignments();
		List<OrganizationalEntity> potentialOwners = new ArrayList<OrganizationalEntity>();

        String actorId = (String) workItem.getParameter("ActorId");
		if (actorId != null) {
			
			String[] actorIds = actorId.split(",");
			for (String id: actorIds) {
				User user = new User();
				user.setId(id.trim());
				potentialOwners.add(user);
			}
            //Set the first user as creator ID??? hmmm might be wrong
            if (potentialOwners.size() > 0){
                taskData.setCreatedBy((User)potentialOwners.get(0));
            }
        }
        String groupId = (String) workItem.getParameter("GroupId");
		if (groupId != null) {
			
			String[] groupIds = groupId.split(",");
			for (String id: groupIds) {

				potentialOwners.add(new Group(id));
			}
			
		}

        assignments.setPotentialOwners(potentialOwners);
		List<OrganizationalEntity> businessAdministrators = new ArrayList<OrganizationalEntity>();
		businessAdministrators.add(new User("Administrator"));
		assignments.setBusinessAdministrators(businessAdministrators);
		task.setPeopleAssignments(assignments);
        
		task.setTaskData(taskData);

		ContentData content = null;
		Object contentObject = workItem.getParameter("Content");
		if (contentObject != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(contentObject);
				out.close();
				content = new ContentData();
				content.setContent(bos.toByteArray());
				content.setAccessType(AccessType.Inline);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		client.addTask(task, content, null);
	}
	
	public void dispose() throws Exception {
		if (client != null) {
			client.disconnect();
		}
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		GetTaskResponseHandler abortTaskResponseHandler =
    		new AbortTaskResponseHandler(client);
    	client.getTaskByWorkItemId(workItem.getId(), abortTaskResponseHandler);
	}
    
    private static class TaskCompletedHandler extends AbstractBaseResponseHandler implements EventResponseHandler {
        private WorkItemManager manager;
        private TaskClient client;
        
        public TaskCompletedHandler(WorkItemManager manager, TaskClient client) {
            this.manager = manager;
            this.client = client;
        }

        public void execute(Payload payload) {
            TaskEvent event = ( TaskEvent ) payload.get();
        	long taskId = event.getTaskId();
            System.out.println("Task completed " + taskId);
        	GetTaskResponseHandler getTaskResponseHandler =
        		new GetCompletedTaskResponseHandler(manager, client);
        	client.getTask(taskId, getTaskResponseHandler);   
        }
        
        public boolean isRemove() {
        	return false;
        }
    }
    
    private static class GetCompletedTaskResponseHandler extends AbstractBaseResponseHandler implements GetTaskResponseHandler {

    	private WorkItemManager manager;
    	private TaskClient client;
    	
    	public GetCompletedTaskResponseHandler(WorkItemManager manager, TaskClient client) {
    		this.manager = manager;
    		this.client = client;
    	}
    	
		public void execute(Task task) {
			long workItemId = task.getTaskData().getWorkItemId();
			if (task.getTaskData().getStatus() == Status.Completed) {
				String userId = task.getTaskData().getActualOwner().getId();
				Map<String, Object> results = new HashMap<String, Object>();
				results.put("ActorId", userId);
				long contentId = task.getTaskData().getOutputContentId();
				if (contentId != -1) {
					GetContentResponseHandler getContentResponseHandler =
						new GetResultContentResponseHandler(manager, task, results);
					client.getContent(contentId, getContentResponseHandler);
				} else {
					manager.completeWorkItem(workItemId, results);
				}
			} else {
				manager.abortWorkItem(workItemId);
			}
		}
    }
    
    private static class GetResultContentResponseHandler extends AbstractBaseResponseHandler implements GetContentResponseHandler {

    	private WorkItemManager manager;
    	private Task task;
    	private Map<String, Object> results;

    	public GetResultContentResponseHandler(WorkItemManager manager, Task task, Map<String, Object> results) {
    		this.manager = manager;
    		this.task = task;
    		this.results = results;
    	}
    	
		public void execute(Content content) {
			ByteArrayInputStream bis = new ByteArrayInputStream(content.getContent());
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(bis);
				Object result = in.readObject();
				in.close();
				results.put("Result", result);
				if (result instanceof Map) {
					Map<?, ?> map = (Map) result;
					for (Map.Entry<?, ?> entry: map.entrySet()) {
						if (entry.getKey() instanceof String) {
							results.put((String) entry.getKey(), entry.getValue());
						}
					}
				}
				manager.completeWorkItem(task.getTaskData().getWorkItemId(), results);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
    }
    
    private static class AbortTaskResponseHandler extends AbstractBaseResponseHandler implements GetTaskResponseHandler {

    	private TaskClient client;
    	
    	public AbortTaskResponseHandler(TaskClient client) {
    		this.client = client;
    	}
    	
		public void execute(Task task) {
			if (task != null) {
				client.skip(task.getId(), "Administrator", null);
			}
		}
    }
    
}
