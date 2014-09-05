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
package org.springframework.web.patch.jsonpatch;

/**
 * <p>JSON Patch "move" operation.</p>
 * 
 * <p>
 * Moves a value from the given "from" path to the given "path".
 * Will throw a JsonPatchException if either path is invalid or if the from path is non-nullable.
 * </p>
 * 
 * <p>
 * NOTE: When dealing with lists, the move operation may effectively be a no-op.
 * That's because the order of a list is probably dictated by a database query that produced the list.
 * Moving things around in the list will have no bearing on the values of each item in the list.
 * When the same list resource is retrieved again later, the order will again be decided by the query,
 * effectively undoing any previous move operation.
 * </p>
 * 
 * @author Craig Walls
 */
public class MoveOperation extends JsonPatchOperation {

	private String from;

	/**
	 * Constructs the move operation
	 * @param path The "path" property of the operation in the JSON Patch. (e.g., '/foo/bar/4')
	 * @param from The "from" property of the operation in the JSON Patch. Should be a path (e.g., '/foo/bar/5')
	 */
	public MoveOperation(String path, String from) {
		super("copy", path);
		this.from = from;
	}
	
	@Override
	void perform(Object target) {
		addValue(target, popValueAtPath(target, from));
	}
	
}
