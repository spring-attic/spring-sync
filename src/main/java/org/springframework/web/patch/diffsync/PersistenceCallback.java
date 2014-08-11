package org.springframework.web.patch.diffsync;

import java.util.List;

/**
 * Callback to handle persistence in the course of applying a patch via Differential Synchronization.
 * Enables DiffSync to be decoupled from any particular persistence mechanism.
 * @author Craig Walls
 *
 * @param <T> The entity type
 */
public interface PersistenceCallback<T> {

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
	
}
