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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Represents a JSON Patch.
 * 
 * @author Craig Walls
 */
public class JsonPatch {

	private final List<JsonPatchOperation> operations;

	private JsonPatch(List<JsonPatchOperation> operations) {
		this.operations = operations;
	}
	
	/**
	 * @return the number of operations that make up this patch.
	 */
	public int size() {
		return operations.size();
	}
	
	/**
	 * Applies the JSON Patch to a given Object graph.
	 * @param in the object graph to apply the patch to.
	 * @return an object graph modified by the patch.
	 * @throws JsonPatchException if there are any errors while applying the patch.
	 */
	public Object apply(Object in) throws JsonPatchException {
		// TODO: Make defensive copy of in before performing operations so that
		//       if any op fails, the original left untouched
		Object work = in; // not really a defensive copy
		
		for (JsonPatchOperation operation : operations) {
			operation.perform(work);
		}

		return work;
	}

	/**
	 * Constructs a JsonPatch object given a JsonNode.
	 * @param jsonNode a JsonNode containing the JSON Patch
	 * @return a JsonPatch
	 */
	public static JsonPatch fromJsonNode(JsonNode jsonNode) {
		if (!(jsonNode instanceof ArrayNode)) {
			throw new IllegalArgumentException("JsonNode must be an instance of ArrayNode");
		}
		
		ArrayNode opNodes = (ArrayNode) jsonNode;
		List<JsonPatchOperation> ops = new ArrayList<JsonPatchOperation>(opNodes.size());
		for(Iterator<JsonNode> elements = opNodes.elements(); elements.hasNext(); ) {
			JsonNode opNode = elements.next();
			
			String opType = opNode.get("op").textValue();
			String path = opNode.get("path").textValue();
			String value = opNode.has("value") ? opNode.get("value").toString() : null;
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
				throw new JsonPatchException("Unrecognized operation type: " + opType);
			}
		}
		
		return new JsonPatch(ops);
	}
	
}
