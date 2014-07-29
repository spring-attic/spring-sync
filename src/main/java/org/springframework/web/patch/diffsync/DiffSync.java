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
package org.springframework.web.patch.diffsync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.springframework.web.patch.jsonpatch.JsonDiff;
import org.springframework.web.patch.jsonpatch.JsonPatch;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Differential Synchronization routine.
 * 
 * @author Craig Walls
 *
 * @param <T> The entity type to perform differential synchronization against.
 */
public class DiffSync<T> {

	private JsonPatch patch;
	
	private ShadowStore shadowStore;
	
	private Class<T> entityType;
	
	private Equivalency sameness = new IdPropertyEquivalency();

	private PersistenceStrategy<List<T>> persistence;
	
	/**
	 * Constructs the Differential Synchronization routine instance.
	 * @param patch a JSON Patch to perform
	 * @param shadowStore the shadow store
	 * @param persistence persistence strategy
	 * @param entityType the entity type
	 */
	public DiffSync(JsonPatch patch, ShadowStore shadowStore, PersistenceStrategy<List<T>> persistence, Class<T> entityType) {
		this.patch = patch;
		this.shadowStore = shadowStore;
		this.persistence = persistence;
		this.entityType = entityType;
	}
	
	public void setSameness(Equivalency sameness) {
		this.sameness = sameness;
	}
	
	public JsonNode apply() {
		List<T> original = find();
		
		String shadowStoreKey = getShadowStoreKey(original);
		
		List<T> source = deepCloneList(original);
		List<T> shadow = (List<T>) shadowStore.getShadow(shadowStoreKey);
		if (shadow == null) {
			shadow = deepCloneList(original);
		}

		if (patch.size() > 0) {
			shadow = (List<T>) patch.apply(shadow);
			source = (List<T>) patch.apply(source);

			List<T> itemsToSave = new ArrayList<T>(source);
			itemsToSave.removeAll(original);

			if (itemsToSave.size() > 0) {
				save(itemsToSave);
			}
	
			// REMOVE ITEMS
			List<T> itemsToRemove = new ArrayList<T>(original);
			for (T candidate : original) {
				for (T item : source) {
					if (isSame(candidate, item)) {
						itemsToRemove.remove(candidate);
						break;
					}
				}
			}
			
			if (itemsToRemove.size() > 0) {
				delete(itemsToRemove);
			}
		}
		
		JsonNode returnPatch = new JsonDiff().diff(shadow, source);
		
		// apply return patch to shadow
		shadow = (List<T>) JsonPatch.fromJsonNode(returnPatch).apply(shadow);
		
		// update session with new shadow
		shadowStore.putShadow(shadowStoreKey, shadow);
		
		return returnPatch;
	}
	
	
	// private helper methods
	
	private String getShadowStoreKey(Object o) {
		if (o instanceof List) {
			return "shadow/list/" + entityType.getSimpleName();
		} else {
			return "shadow/" + entityType.getSimpleName();
		}
	}
	
	private boolean isSame(T o1, T o2) {
		return sameness.isEquivalent(o1, o2);
	}
	
	private List<T> find() {
		return persistence.find();
	}
	
	private void save(List<T> list) {
		if (list.size() > 0) {
			persistence.save(list);
		}
	}
	
	private void delete(List<T> list) {
		if (list.size() > 0) {
			persistence.delete(list);
		}
	}

	private List<T> deepCloneList(List<T> original) {
		List<T> copy = new ArrayList<T>(original.size());
		for(T t : original) {
			// TODO : Hokeyness in the following line should be addressed
			copy.add((T) SerializationUtils.clone((Serializable) t)); 
		}
		return copy;
	}

}
