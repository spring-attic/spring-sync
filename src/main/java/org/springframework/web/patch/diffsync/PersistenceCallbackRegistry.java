package org.springframework.web.patch.diffsync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceCallbackRegistry {

	private Map<String, PersistenceCallback<?>> persistenceCallbacks;
	
	public PersistenceCallbackRegistry(List<PersistenceCallback<?>> persistenceCallbacks) {
		this.persistenceCallbacks = new HashMap<String, PersistenceCallback<?>>();
		
		for (PersistenceCallback<?> persistenceCallback : persistenceCallbacks) {
			Class<?> entityType = persistenceCallback.getEntityType();
			String key = entityType.getSimpleName().toLowerCase() + "s"; // TODO - Naive approach to pluralization
			this.persistenceCallbacks.put(key, persistenceCallback);
		}
	}
	
	public PersistenceCallback<?> findPersistenceCallback(String key) {
		return persistenceCallbacks.get(key);
	}
	
}
