package org.springframework.patch.diffsync.web;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.patch.diffsync.PersistenceCallback;

class JpaPersistenceCallback<T> implements PersistenceCallback<T> {
	
	private final CrudRepository<T, Long> repo;
	private Class<T> entityType;

	public JpaPersistenceCallback(CrudRepository<T, Long> repo, Class<T> entityType) {
		this.repo = repo;
		this.entityType = entityType;
	}
	
	@Override
	public List<T> findAll() {
		return (List<T>) repo.findAll();
	}
	
	@Override
	public void persistChange(T itemToSave) {
		repo.save(itemToSave);
	}
	
	@Override
	public void persistChanges(List<T> itemsToSave, List<T> itemsToDelete) {
		repo.save(itemsToSave);
		repo.delete(itemsToDelete);
	}

	@Override
	public Class<T> getEntityType() {
		return entityType;
	}
	
}