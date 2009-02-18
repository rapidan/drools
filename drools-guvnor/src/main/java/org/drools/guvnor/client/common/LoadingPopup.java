package org.drools.guvnor.client.common;
/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.MessageBoxConfig;
import com.google.gwt.core.client.GWT;
import org.drools.guvnor.client.messages.Constants;


/**
 * Generic "busy" dialog popup.
 * This is a lazy singleton, only really need one to be shown at time.
 */
public class LoadingPopup  {

    public static LoadingPopup instance = null;



    private LoadingPopup() {
    }


    /**
     * Close the single instance of this dialog...
     */
    public static void close() {
        MessageBox.hide();
    }



    public static LoadingPopup getInstance() {
        if (instance == null) {
            instance = new LoadingPopup();
        }
        return instance;
    }

    /** Convenience method to popup the message. */
    public static void showMessage(final String message) {
    	MessageBox.show(new MessageBoxConfig() {
    		{
    			setTitle(((Constants) GWT.create(Constants.class)).PleaseWaitDotDotDot());
    			setWidth(200);
    			setMsg(message);
    			setClosable(true);
    		}
    	});
    }






}