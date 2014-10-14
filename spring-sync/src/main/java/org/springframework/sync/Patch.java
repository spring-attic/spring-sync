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
package org.springframework.sync;

import java.util.List;

/**
 * <p>Represents a Patch.</p>
 * 
 * <p>
 * This class (and {@link PatchOperation} capture the definition of a patch, but are not coupled
 * to any specific patch representation. 
 * </p>
 * 
 * @author Craig Walls
 */
public class Patch {

	private final List<PatchOperation> operations;

	public Patch(List<PatchOperation> operations) {
		this.operations = operations;
	}
	
	/**
	 * @return the number of operations that make up this patch.
	 */
	public int size() {
		return operations.size();
	}
	
	public List<PatchOperation> getOperations() {
		return operations;
	}
	
	/**
	 * Applies the Patch to a given Object graph.
	 * @param in the object graph to apply the patch to.
	 * @return an object graph modified by the patch.
	 * @throws PatchException if there are any errors while applying the patch.
	 */
	public <T> T apply(T in) throws PatchException {
		// TODO: Make defensive copy of in before performing operations so that
		//       if any op fails, the original left untouched
		T work = in; // TODO: This is not really a defensive copy; just a placeholder
		
		for (PatchOperation operation : operations) {
			operation.perform(work);
		}

		return work;
	}

}
