/**
 *
 */
package org.drools.task.service.responsehandlers;

import org.drools.task.service.TaskClientHandler.DeleteCommentResponseHandler;

public class BlockingDeleteCommentResponseHandler extends AbstractBlockingResponseHandler implements DeleteCommentResponseHandler {
    public void setIsDone(boolean done) {
        setDone(done);
    }
}