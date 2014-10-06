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

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;
import difflib.Patch;

public class JsonDiff {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private JsonNodeFactory nodeFactory = new JsonNodeFactory(true);;

	public JsonNode diff(Object original, Object modified) throws PatchException {
		try {
			ArrayNode patch = nodeFactory.arrayNode();
			
			if (original instanceof List && modified instanceof List) {
				diffList(patch, "", (List<?>) original, (List<?>) modified);
			} else {
				diffNonList(patch, "", original, modified);
			}
			
			return patch;
		} catch (Exception e) {
			throw new PatchException("Error performing diff:", e);
		}
	}
	
	private void diffList(ArrayNode patch, String path, List<?> original, List<?> modified) throws IOException, IllegalAccessException {
	
		Patch diff = DiffUtils.diff(original, modified);
		List<Delta> deltas = diff.getDeltas();
		for (Delta delta : deltas) {
			TYPE type = delta.getType();
			int revisedPosition = delta.getRevised().getPosition();
			if (type == TYPE.CHANGE) {
				List<?> lines = delta.getRevised().getLines();
				for(int offset = 0; offset < lines.size(); offset++) {
					Object originalObject = original.get(revisedPosition + offset);
					Object revisedObject = modified.get(revisedPosition + offset);
					diffNonList(patch, path + "/" + (revisedPosition + offset), originalObject, revisedObject);					
				}
				
			} else if (type == TYPE.INSERT) {
				List<?> lines = delta.getRevised().getLines();
				for(int offset = 0; offset < lines.size(); offset++) {
					patch.add(opNode("add", path + "/" + (revisedPosition + offset), toJsonString(lines.get(offset)), null));
				}
			} else if (type == TYPE.DELETE) {
				List<?> lines = delta.getOriginal().getLines();
				for(int offset = 0; offset < lines.size(); offset++) {
					Object originalObject = original.get(revisedPosition + offset);
					patch.add(opNode("test", path + "/" + revisedPosition, toJsonString(originalObject),null));
					patch.add(opNode("remove", path + "/" + revisedPosition, null, null));
				}
			}
		}
	}
	
	private void diffNonList(ArrayNode patch, String path, Object original, Object modified) throws IOException, IllegalAccessException {
		
		if (!ObjectUtils.nullSafeEquals(original, modified)) {
			if (modified == null) {
				patch.add(opNode("remove", path, null, null));
				return;
			}
			
			if (isPrimitive(modified)) {
				String modValue = modified != null ? toJsonString(modified) : null;
				String origValue = original != null ? toJsonString(original) : null;
				
				patch.add(opNode("test", path, origValue, null));
				if (origValue == null) {
					patch.add(opNode("add", path, modValue, null));
				} else {
					patch.add(opNode("replace", path, modValue, null));					
				}
				return;
			}
						
			Class<? extends Object> originalType = original.getClass();
			Field[] fields = originalType.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Object origValue = field.get(original);
				Object modValue = field.get(modified);
				diffNonList(patch, path+"/"+field.getName(), origValue, modValue);
			}
			
		}
		
	}

	private String toJsonString(Object modified) throws IOException, JsonProcessingException {
		StringWriter writer = new StringWriter();
		MAPPER.writeValue(writer,  modified);
		return writer.toString();
	}
	
	private ObjectNode opNode(String op, String path, String value, String from) {
		ObjectNode opNode = nodeFactory.objectNode();
		opNode.set("op", nodeFactory.textNode(op));
		opNode.set("path", nodeFactory.textNode(path));
		if (value != null) {
			try {
				opNode.set("value", MAPPER.readTree(value));
			} catch (Exception e) {
				// TODO : DEAL WITH THIS EXCEPTION BETTER
			}
		}
		if (from != null) {
			opNode.set("from", nodeFactory.textNode(from));
		}
		return opNode;
	}
	
	private boolean isPrimitive(Object o) {
		return o instanceof String || o instanceof Number || o instanceof Boolean;
	}
	
}
