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
import org.springframework.data.repository.CrudRepository;
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
	
	private Equivalency equivalency = new IdPropertyEquivalency();

	// TODO: Try to extract this out. Do the save/delete independent of and after the diff sync routine.
	private CrudRepository<T, ?> repository;

	/**
	 * Constructs the Differential Synchronization routine instance.
	 * @param patch a JSON Patch to perform
	 * @param shadowStore the shadow store
	 * @param repository Spring Data {@link CrudRepository} to save and delete as necessary in the course of applying the patch
	 * @param entityType the entity type
	 */
	public DiffSync(JsonPatch patch, ShadowStore shadowStore, CrudRepository<T, ?> repository, Class<T> entityType) {
		this.repository = repository;
		this.patch = patch;
		this.shadowStore = shadowStore;
		this.entityType = entityType;
	}
	
	/**
	 * Sets the equivalency check strategy.
	 * Used to compare two objects to determine if they represent the same entity, even if the values of their properties differ.
	 * Defaults to {@link IdPropertyEquivalency} which compares the id property of each object.
	 * @param equivalency the equivalency strategy.
	 */
	public void setEquivalency(Equivalency equivalency) {
		this.equivalency = equivalency;
	}
	
	/**
	 * Applies the patch to a single, non-list object.
	 * @param target the object to apply a patch to.
	 * @return a {@link JsonNode} containing a JSON Patch to apply to the source of the target object (e.g., to send back to the client).
	 */
	@SuppressWarnings("unchecked")
	public JsonNode apply(T target) {
		// Clone the target into a working copy so that we can diff it later.
		// Must be a deep clone or else items in any list properties will be the exact same instances
		// and could be changed in both the target and the working copy.
		T workCopy = deepClone(target);
		
		// Look up the shadow from the shadow store; clone the target if the shadow doesn't exist.
		String shadowStoreKey = getShadowStoreKey(target);
		T shadow = (T) shadowStore.getShadow(shadowStoreKey);
		if (shadow == null) {
			shadow = deepClone(target);
		}
		
		// Apply patch to both shadow and working copy.
		if (patch.size() > 0) {
			shadow = (T) patch.apply(shadow);
			workCopy = (T) patch.apply(workCopy);
			
			// Save the working copy.
			repository.save(workCopy);
		}
		
		// Calculate the return patch by diff'ing the shadow and working copy.
		JsonNode returnPatch = new JsonDiff().diff(shadow, workCopy);
		
		// Store the shadow.
		shadowStore.putShadow(shadowStoreKey, shadow);
		
		// Return the return patch.
		return returnPatch;
	}
	
	/**
	 * Applies the patch to a list.
	 * @param target the list to apply a patch to.
	 * @return a {@link JsonNode} containing a JSON Patch to apply to the source of the target object (e.g., to send back to the client).
	 */
	@SuppressWarnings("unchecked")
	public JsonNode apply(List<T> target) {
		// Clone the target into a working copy so that we can diff it later.
		// Must be a deep clone or else the individual items in the list will still be the exact same instances
		// and could be changed in both the target and the working copy.
		List<T> workCopy = deepCloneList(target);
		
		// Look up the shadow from the shadow store; clone the target if the shadow doesn't exist.
		String shadowStoreKey = getShadowStoreKey(target);
		List<T> shadow = (List<T>) shadowStore.getShadow(shadowStoreKey);
		if (shadow == null) {
			shadow = deepCloneList(target);
		}

		if (patch.size() > 0) {
			// Apply patch to both shadow and working copy.
			shadow = (List<T>) patch.apply(shadow);
			workCopy = (List<T>) patch.apply(workCopy);

			// Determine which items changed.
			// Make a shallow copy of the working copy, remove items that are in the target.
			// What's left are the items that changed and need to be saved.
			List<T> itemsToSave = new ArrayList<T>(workCopy);
			itemsToSave.removeAll(target);

			// Save the changed items
			if (itemsToSave.size() > 0) {
				repository.save(itemsToSave);
			}
	
			// Determine which items should be deleted.
			// Make a shallow copy of the target, remove items that are equivalent to items in the working copy.
			// Equivalent is not the same as equals. It means "this is the same resource, even if it has changed".
			// It usually means "are the id properties equals".
			List<T> itemsToRemove = new ArrayList<T>(target);
			for (T candidate : target) {
				for (T item : workCopy) {
					if (equivalency.isEquivalent(candidate, item)) {
						itemsToRemove.remove(candidate);
						break;
					}
				}
			}
			
			// Delete the items that were deleted as part of the patch
			if (itemsToRemove.size() > 0) {
				repository.delete(itemsToRemove);
			}
		}
		
		// Calculate the return patch by diff'ing the shadow and working copy
		JsonNode returnPatch = new JsonDiff().diff(shadow, workCopy);
		
		// Apply the return patch to the shadow to sync it up with the working copy.
		shadow = (List<T>) JsonPatch.fromJsonNode(returnPatch).apply(shadow);
		
		// Store the shadow
		shadowStore.putShadow(shadowStoreKey, shadow);
		
		// Return the patch
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
	
	@SuppressWarnings("unchecked")
	private T deepClone(T original) {
		return (T) SerializationUtils.clone((Serializable) original);
	}
	
	@SuppressWarnings("unchecked")
	private List<T> deepCloneList(List<T> original) {
		List<T> copy = new ArrayList<T>(original.size());
		for(T t : original) {
			// TODO : Hokeyness in the following line should be addressed
			copy.add((T) SerializationUtils.clone((Serializable) t)); 
		}
		return copy;
	}

}
