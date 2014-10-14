/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.sync.diffsync;

import java.util.HashMap;
import java.util.Map;

public class PersistenceCallbackRegistry {

	private Map<String, PersistenceCallback<?>> persistenceCallbacks = new HashMap<String, PersistenceCallback<?>>();
	
	public PersistenceCallbackRegistry() {}
	
	public void addPersistenceCallback(PersistenceCallback<?> persistenceCallback) {
		Class<?> entityType = persistenceCallback.getEntityType();
		String key = entityType.getSimpleName().toLowerCase() + "s"; // TODO - Naive approach to pluralization
		persistenceCallbacks.put(key, persistenceCallback);
	}
	
	public PersistenceCallback<?> findPersistenceCallback(String key) {
		return persistenceCallbacks.get(key);
	}
	
}
