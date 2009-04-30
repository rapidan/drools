/**
 *
 */
package org.drools.process.workitem.wsht;

import org.drools.task.service.TaskClientHandler.AddTaskResponseHandler;
import org.drools.task.service.responsehandlers.AbstractBlockingResponseHandler;

public class BlockingAddTaskResponseHandler extends AbstractBlockingResponseHandler implements AddTaskResponseHandler {
    private static final int DEFAULT_WAIT_TIME = 10000;
    
    private volatile long taskId;

    public synchronized void execute(long taskId) {
        this.taskId = taskId;
        setDone(true);
    }

    public long getTaskId() {
        // note that this method doesn't need to be synced because if waitTillDone returns true,
        // it means taskId is available 
        boolean done = waitTillDone(DEFAULT_WAIT_TIME);

        if (!done) {
            throw new RuntimeException("Timeout : unable to retrieve Task Id");
        }

        return taskId;
    }
}