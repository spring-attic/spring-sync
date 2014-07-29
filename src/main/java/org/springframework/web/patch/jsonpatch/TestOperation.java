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

import java.io.IOException;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>JSON Patch "test" operation.</p>
 * 
 * <p>
 * If the value given matches the value given at the path, the operation completes as a no-op.
 * On the other hand, if the values do not match or if there are any errors interpreting the path,
 * a JsonPatchException will be thrown.
 * </p>
 * 
 * @author Craig Walls
 */
public class TestOperation extends JsonPatchOperation {

	private final String value;

	/**
	 * Constructs the test operation
	 * @param path The "path" property of the operation in the JSON Patch. (e.g., '/foo/bar/4')
	 * @param value The "value" property of the operation in the JSON Patch. The String value should contain valid JSON.
	 */
	public TestOperation(String path, String value) {
		super("test", path);
		this.value = value;
	}
	
	@Override
	void perform(Object targetObject) {
		Object targetValue = getValue(targetObject);
		
		// targetValue could be null
		
		try {
			// TODO: This conversion will prove useful in other operations, so it should probably be made part of the parent type
			ObjectMapper mapper = new ObjectMapper();
			
			Class<?> targetType = targetValue != null ? targetValue.getClass() : Object.class;
			Object comparisonValue = value != null ? mapper.readValue(value.toString(), targetType) : null;
			if (!ObjectUtils.nullSafeEquals(comparisonValue, targetValue)) {
				throw new JsonPatchException("Test against path '" + path + "' failed");
			}
		} catch (IOException e) {
			throw new JsonPatchException("Test against path '" + path + "' failed.");
		}
	}
	
}
