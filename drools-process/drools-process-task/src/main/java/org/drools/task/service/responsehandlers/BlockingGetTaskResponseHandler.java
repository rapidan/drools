/**
 *
 */
package org.drools.task.service.responsehandlers;

import org.drools.task.Task;
import org.drools.task.service.TaskClientHandler.GetTaskResponseHandler;

public class BlockingGetTaskResponseHandler extends AbstractBlockingResponseHandler implements GetTaskResponseHandler {
    private static final int TASK_WAIT_TIME = 10000;

    private volatile Task task;

    public synchronized void execute(Task task) {
        this.task = task;
        setDone(true);
    }

    public Task getTask() {
        // note that this method doesn't need to be synced because if waitTillDone returns true,
        // it means task is available 
        boolean done = waitTillDone(TASK_WAIT_TIME);

        if (!done) {
            throw new RuntimeException("Timeout : unable to retrieve Task");
        }

        return task;
    }
}