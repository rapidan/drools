package org.drools.guvnor.server.security;
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



import java.util.List;
import java.io.Serializable;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;

/**
 * This enhances the BRMS repository for lifecycle management.
 * @author Chester Woo/Jervis Liu
 */
@Scope(ScopeType.SESSION)
@AutoCreate
@Name("roleBasedPermissionManager")
public class RoleBasedPermissionManager implements Serializable {
    
//    @In 
    List<RoleBasedPermission> permissions;
    
//    @Unwrap
    public List<RoleBasedPermission> getRoleBasedPermission() {
		return permissions;
	}  
    
    @Create
	public void create() {
		String userName = "";
		if (Contexts.isApplicationContextActive()) {
			userName = Identity.instance().getCredentials().getUsername();
		}

		RoleBasedPermissionStore pbps = (RoleBasedPermissionStore) Component
				.getInstance("org.drools.guvnor.server.security.RoleBasedPermissionStore");
		permissions = pbps.getRoleBasedPermissionsByUserName(Identity
				.instance().getCredentials().getUsername());

	}
    
    @Destroy
    public void close() {
            
    }
    

    
}
