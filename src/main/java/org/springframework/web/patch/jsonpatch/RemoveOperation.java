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
 * <p>JSON Patch "remove" operation.</p>
 * 
 * <p>
 * Removes the value at the given path.
 * Will throw a JsonPatchException if the given path isn't valid or if the path is non-nullable.
 * </p>
 * 
 * @author Craig Walls
 */
public class RemoveOperation extends JsonPatchOperation {

	/**
	 * Constructs the remove operation
	 * @param path The "path" property of the operation in the JSON Patch. (e.g., '/foo/bar/4')
	 */
	public RemoveOperation(String path) {
		super("remove", path);
	}
	
	@Override
	void perform(Object target) {
		popValueAtPath(target, path);
	}

}
