/*
 * Copyright 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.brms.client;

import com.google.gwt.user.client.ui.Image;

/**
 * Demonstrates {@link com.google.gwt.user.client.ui.MenuBar} and
 * {@link com.google.gwt.user.client.ui.MenuItem}.
 */
public class Deployment extends JBRMSFeature {

  public static ComponentInfo init() {
    return new ComponentInfo("Deployment",
      "Deploy rulebases to runtime systems.") {
      public JBRMSFeature createInstance() {
        return new Deployment();
      }

	public Image getImage() {	
		return new Image("images/deploy.gif");
	}
    };
  }


  public Deployment() {

  }



  public void onShow() {
  }
}
