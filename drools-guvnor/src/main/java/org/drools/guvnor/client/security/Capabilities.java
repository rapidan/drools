package org.drools.guvnor.client.security;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This is used to turn off GUI functionality. The server decides what should be visible
 * based on roles and permissions granted. This is essentially a security and permissions function.
 * (however the Capabilities do not enforce actions on the server - these are more for GUI convenience so elements are not displayed
 * that are not relevant to a given users role).
 *
 * @author Michael Neale
 *
 */
public class Capabilities implements IsSerializable {


	public static final Integer SHOW_PACKAGE_VIEW = 1;  //(show status list view as well) if they have any package perms
	public static final Integer SHOW_CREATE_NEW_ASSET = 2; //if they have any package perms not read only
	public static final Integer SHOW_CREATE_NEW_PACKAGE = 3; //if they are package admin
	public static final Integer	SHOW_ADMIN = 4; //if they are admin, package admin??
	public static final Integer	SHOW_QA = 5; //if they have any package perms
	public static final Integer	SHOW_DEPLOYMENT = 6; //if they are package admin??
	public static final Integer SHOW_DEPLOYMENT_NEW = 7; //can create a new depl, rename etc...

	public List<Integer> list = new ArrayList<Integer>();

    /**
     * This is the list of features that are enabled.
     */
    public Map<String, String> prefs = new HashMap<String, String>();

	/**
	 * Grants all capabilities.
	 * Only used for when there is basically no login.
	 */
	public static Capabilities all(Map<String, String> prefs) {

		Capabilities cp = new Capabilities();
		cp.list.add(SHOW_PACKAGE_VIEW);
		cp.list.add(SHOW_CREATE_NEW_ASSET);
		cp.list.add(SHOW_CREATE_NEW_PACKAGE);
		cp.list.add(SHOW_ADMIN);
		cp.list.add(SHOW_QA);
		cp.list.add(SHOW_DEPLOYMENT);
		cp.list.add(SHOW_DEPLOYMENT_NEW);
        cp.prefs = prefs;

		return cp;
	}

}
