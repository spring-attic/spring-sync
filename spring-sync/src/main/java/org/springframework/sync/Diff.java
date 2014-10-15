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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ObjectUtils;

import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;

/**
 * Provides support for producing a {@link Patch} from the comparison of two objects.
 * @author Craig Walls
 */
public class Diff {

	/**
	 * Performs a difference operation between two objects, resulting in a {@link Patch} describing the differences.
	 * 
	 * @param original the original, unmodified object.
	 * @param modified the modified object.
	 * @return a {@link Patch} describing the differences between the two objects.
	 * @throws PatchException if an error occurs while performing the difference.
	 */
	public Patch diff(Object original, Object modified) throws PatchException {
		try {
			List<PatchOperation> operations = new ArrayList<PatchOperation>();
			if (original instanceof List && modified instanceof List) {
				diffList(operations, "", (List<?>) original, (List<?>) modified);
			} else {
				diffNonList(operations, "", original, modified);
			}
			
			return new Patch(operations);
		} catch (Exception e) {
			throw new PatchException("Error performing diff:", e);
		}
	}
	
	// private helpers
	
	private void diffList(List<PatchOperation> operations, String path, List<?> original, List<?> modified) throws IOException, IllegalAccessException {
	
		difflib.Patch diff = DiffUtils.diff(original, modified);
		List<Delta> deltas = diff.getDeltas();
		for (Delta delta : deltas) {
			TYPE type = delta.getType();
			int revisedPosition = delta.getRevised().getPosition();
			if (type == TYPE.CHANGE) {
				List<?> lines = delta.getRevised().getLines();
				for(int offset = 0; offset < lines.size(); offset++) {
					Object originalObject = original.get(revisedPosition + offset);
					Object revisedObject = modified.get(revisedPosition + offset);
					diffNonList(operations, path + "/" + (revisedPosition + offset), originalObject, revisedObject);					
				}
				
			} else if (type == TYPE.INSERT) {
				List<?> lines = delta.getRevised().getLines();
				for(int offset = 0; offset < lines.size(); offset++) {
					operations.add(new AddOperation(path + "/" + (revisedPosition + offset), lines.get(offset)));
				}
			} else if (type == TYPE.DELETE) {
				List<?> lines = delta.getOriginal().getLines();
				for(int offset = 0; offset < lines.size(); offset++) {
					Object originalObject = original.get(revisedPosition + offset);
					operations.add(new TestOperation(path + "/" + revisedPosition, originalObject));
					operations.add(new RemoveOperation(path + "/" + revisedPosition));
				}
			}
		}
	}
	
	private void diffNonList(List<PatchOperation> operations, String path, Object original, Object modified) throws IOException, IllegalAccessException {
		
		if (!ObjectUtils.nullSafeEquals(original, modified)) {
			if (modified == null) {
				operations.add(new RemoveOperation(path));
				return;
			}
			
			if (isPrimitive(modified)) {
				
				operations.add(new TestOperation(path, original));
				if (original == null) {
					operations.add(new AddOperation(path, modified));
				} else {
					operations.add(new ReplaceOperation(path, modified));
				}
				return;
			}
						
			Class<? extends Object> originalType = original.getClass();
			Field[] fields = originalType.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Object origValue = field.get(original);
				Object modValue = field.get(modified);
				diffNonList(operations, path+"/"+field.getName(), origValue, modValue);
			}
			
		}
		
	}

	private boolean isPrimitive(Object o) {
		return o instanceof String || o instanceof Number || o instanceof Boolean;
	}
	
}
