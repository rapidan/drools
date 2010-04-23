/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.grid.local;

import java.util.HashMap;
import java.util.Map;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.runtime.CommandExecutor;

/**
 *
 * @author salaboy
 */
public class DirectoryLookupProviderLocalClient implements DirectoryLookupFactoryService {

    
    private Map<String, CommandExecutor> services = new HashMap<String, CommandExecutor>();

    public DirectoryLookupProviderLocalClient() {
        
    }


    public void register(String key, CommandExecutor executor) {
            services.put(key, executor);
    }

    public CommandExecutor lookup(String key) {
            return  services.get(key);
    }

    public Map<String, Map<String, String>> getDirectoryMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
