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

import java.util.List;

/**
 * Callback to handle persistence in the course of applying a patch via Differential Synchronization.
 * Enables DiffSyncController to be decoupled from any particular persistence mechanism.
 * @author Craig Walls
 *
 * @param <T> The entity type
 */
public interface PersistenceCallback<T> {

	/**
	 * Find all instances of the entity
	 * @return all instance of the entity
	 */
	List<T> findAll();
	
	/**
	 * find a single entity
	 * @param id the id of the entity as a String. The implementation may convert it to the actual type.
	 * @return the entity
	 */
	T findOne(String id);

	/**
	 * Save a single item.
	 * @param itemToSave the item to save.
	 */
	void persistChange(T itemToSave);
	
	/**
	 * Save changed items and delete removed items.
	 * @param itemsToSave a list of items to be saved.
	 * @param itemsToDelete a list of items to be deleted.
	 */
	void persistChanges(List<T> itemsToSave, List<T> itemsToDelete);
		
	/**
	 * @return the type of entity that this callback works with.
	 */
	Class<T> getEntityType();
	
}
