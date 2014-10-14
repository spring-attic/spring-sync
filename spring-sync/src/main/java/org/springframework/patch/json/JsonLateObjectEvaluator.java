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
package org.springframework.patch.json;

import static org.springframework.patch.PathToSpEL.*;

import java.util.List;

import org.springframework.expression.Expression;
import org.springframework.patch.LateObjectEvaluator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link LateObjectEvaluator} implementation that assumes values represented as JSON objects.
 * @author Craig Walls
 */
class JsonLateObjectEvaluator implements LateObjectEvaluator {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private String path;
	private JsonNode valueNode;

	public JsonLateObjectEvaluator(String path, JsonNode valueNode) {
		this.path = path;
		this.valueNode = valueNode;
	}
	
	@Override
	public Object evaluate(Object target) {
		try {
			Expression parentExpression = pathToParentExpression(path);
			Object parent = parentExpression != null ? parentExpression.getValue(target) : null;
			Integer listIndex = targetListIndex(path);
			if (parent == null || !(parent instanceof List) || listIndex == null) {
				// TODO: What to do if the target isn't a list???
				// TODO: Look at target type and use that to guess
				// TODO: Need a test around this
			} else {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) parentExpression.getValue(target);
				Class<?> guessedType = guessListType(list);
				return MAPPER.readValue(valueNode.traverse(), guessedType);
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	// TODO: Duplicates same method in PatchOperation
	private Integer targetListIndex(String path) {
		String[] pathNodes = path.split("\\/");
		try {
			return Integer.parseInt(pathNodes[pathNodes.length - 1]);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Class<?> guessListType(List<?> list) {
		// TODO: Guess assumes a non-empty list.
		//       If the list is empty, then this won't work.
		//       It also assumes a homogeneous list. If there are mixed types in the list, it won't work.
		//
		//       Other ways to guess:
		//       - If the list is a declared property, introspection might work
		//       - JsonPatchOperation could be explicitly given the type

		return !list.isEmpty() ? list.get(0).getClass() : null;
	}

}
