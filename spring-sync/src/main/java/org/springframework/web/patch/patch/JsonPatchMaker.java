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
package org.springframework.web.patch.patch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonPatchMaker {

	/**
	 * Constructs a JsonPatch object given a JsonNode.
	 * @param jsonNode a JsonNode containing the JSON Patch
	 * @return a JsonPatch
	 */
	public Patch fromJsonNode(JsonNode jsonNode) {
		if (!(jsonNode instanceof ArrayNode)) {
			throw new IllegalArgumentException("JsonNode must be an instance of ArrayNode");
		}
		
		ArrayNode opNodes = (ArrayNode) jsonNode;
		List<PatchOperation> ops = new ArrayList<PatchOperation>(opNodes.size());
		for(Iterator<JsonNode> elements = opNodes.elements(); elements.hasNext(); ) {
			JsonNode opNode = elements.next();
			
			String opType = opNode.get("op").textValue();
			String path = opNode.get("path").textValue();
			
			JsonNode valueNode = opNode.get("value");
			Object value = valueFromJsonNode(path, valueNode);
			// TODO: Pass value to the operations instead of valueString
			
			String from = opNode.has("from") ? opNode.get("from").textValue() : null;

			if (opType.equals("test")) {
				ops.add(new TestOperation(path, value));
			} else if (opType.equals("replace")) {
				ops.add(new ReplaceOperation(path, value));
			} else if (opType.equals("remove")) {
				ops.add(new RemoveOperation(path));
			} else if (opType.equals("add")) {
				ops.add(new AddOperation(path, value));
			} else if (opType.equals("copy")) {
				ops.add(new CopyOperation(path, from));
			} else if (opType.equals("move")) {
				ops.add(new MoveOperation(path, from));
			} else {
				throw new PatchException("Unrecognized operation type: " + opType);
			}
		}
		
		return new Patch(ops);
	}
	
	public JsonNode toJsonNode(Patch patch) {
		return null; // TODO
	}

	private Object valueFromJsonNode(String path, JsonNode valueNode) {
		if (valueNode == null || valueNode.isNull()) {
			return null;
		} else if (valueNode.isTextual()) {
			return valueNode.asText();
		} else if (valueNode.isFloatingPointNumber()) {
			return valueNode.asDouble();
		} else if (valueNode.isBoolean()) {
			return valueNode.asBoolean();
		} else if (valueNode.isInt()) {
			return valueNode.asInt();
		} else if (valueNode.isLong()) {
			return valueNode.asLong();
		}

		// TODO: Must be an object or an array
		
		if (valueNode.isObject()) {
			System.out.println("IT'S AN OBJECT!");
			return new JsonLateObjectEvaluator(path, valueNode);
		} else if (valueNode.isArray()) {
			// TODO: Convert valueNode to array
		}
		
		return null;
		
		
//		Object parent = parentExpression != null ? parentExpression.getValue(target) : null;
//		Integer listIndex = targetListIndex(path);
//		if (parent == null || !(parent instanceof List) || listIndex == null) {
//			spelExpression.setValue(target, value);
//		} else {
//			@SuppressWarnings("unchecked")
//			List<Object> list = (List<Object>) parentExpression.getValue(target);
//			Class<?> guessedType = guessListType(list);
//			Object newItem = MAPPER.readValue((String) value, guessedType);
//		}
	}
	
	

	
}
