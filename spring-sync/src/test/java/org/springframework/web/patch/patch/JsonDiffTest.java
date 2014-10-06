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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.web.patch.Todo;
import org.springframework.web.patch.patch.JsonDiff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonDiffTest {

	@Test
	public void noChanges() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();

		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode arrayNode = (ArrayNode) diff;
		assertEquals(0, arrayNode.size());
	}
	
	@Test
	public void nullPropertyToNonNullProperty() throws Exception {
		Todo original = new Todo(null, "A", false);
		Todo modified = new Todo(1L, "A", false);		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/id", test.get("path").textValue());
		assertEquals(null, test.get("value"));
	}
	
	@Test
	public void singleBooleanPropertyChangeOnObject() throws Exception {
		Todo original = new Todo(1L, "A", false);
		Todo modified = new Todo(1L, "A", true);
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/complete", test.get("path").textValue());
		assertFalse(test.get("value").booleanValue());
		JsonNode replace = patch.get(1);
		assertEquals("replace", replace.get("op").textValue());
		assertEquals("/complete", replace.get("path").textValue());
		assertTrue(replace.get("value").booleanValue());
	}
	
	@Test
	public void singleStringPropertyChangeOnObject() throws Exception {
		Todo original = new Todo(1L, "A", false);
		Todo modified = new Todo(1L, "B", false);
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/description", test.get("path").textValue());
		assertEquals("A", test.get("value").textValue());
		JsonNode op = patch.get(1);
		assertEquals("replace", op.get("op").textValue());
		assertEquals("/description", op.get("path").textValue());
		assertEquals("B", op.get("value").textValue());
	}

	@Test
	public void singleNumericPropertyChangeOnObject() throws Exception {
		Todo original = new Todo(1L, "A", false);
		Todo modified = new Todo(2L, "A", false);
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/id", test.get("path").textValue());
		assertEquals(1, test.get("value").longValue());
		JsonNode op = patch.get(1);
		assertEquals("replace", op.get("op").textValue());
		assertEquals("/id", op.get("path").textValue());
		assertEquals(2, op.get("value").longValue());
	}
	
	@Test
	public void changeTwoPropertiesOnObject() throws Exception {
		Todo original = new Todo(1L, "A", false);
		Todo modified = new Todo(1L, "B", true);
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(4, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/description", test.get("path").textValue());
		assertEquals("A", test.get("value").textValue());
		JsonNode op = patch.get(1);
		assertEquals("replace", op.get("op").textValue());
		assertEquals("/description", op.get("path").textValue());
		assertEquals("B", op.get("value").textValue());
		JsonNode test2 = patch.get(2);
		assertEquals("test", test2.get("op").textValue());
		assertEquals("/complete", test2.get("path").textValue());
		assertFalse(test2.get("value").booleanValue());
		JsonNode replace = patch.get(3);
		assertEquals("replace", replace.get("op").textValue());
		assertEquals("/complete", replace.get("path").textValue());
		assertTrue(replace.get("value").booleanValue());		
	}
	
	@Test
	public void singleBooleanPropertyChangeOnItemInList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.get(1).setComplete(true);
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/1/complete", test.get("path").textValue());
		assertFalse(test.get("value").booleanValue());
		JsonNode replace = patch.get(1);
		assertEquals("replace", replace.get("op").textValue());
		assertEquals("/1/complete", replace.get("path").textValue());
		assertTrue(replace.get("value").booleanValue());
	}

	@Test
	public void singleStringPropertyChangeOnItemInList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.get(1).setDescription("BBB");
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/1/description", test.get("path").textValue());
		assertEquals("B", test.get("value").textValue());
		JsonNode replace = patch.get(1);
		assertEquals("replace", replace.get("op").textValue());
		assertEquals("/1/description", replace.get("path").textValue());
		assertEquals("BBB", replace.get("value").textValue());
	}

	@Test
	public void singleMultiplePropertyChangeOnItemInList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.get(1).setComplete(true);
		modified.get(1).setDescription("BBB");
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(4, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/1/description", test.get("path").textValue());
		assertEquals("B", test.get("value").textValue());
		JsonNode replace = patch.get(1);
		assertEquals("replace", replace.get("op").textValue());
		assertEquals("/1/description", replace.get("path").textValue());
		assertEquals("BBB", replace.get("value").textValue());
		test = patch.get(2);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/1/complete", test.get("path").textValue());
		assertFalse(test.get("value").booleanValue());
		replace = patch.get(3);
		assertEquals("replace", replace.get("op").textValue());
		assertEquals("/1/complete", replace.get("path").textValue());
		assertTrue(replace.get("value").booleanValue());
	}

	@Test
	public void propertyChangeOnTwoItemsInList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.get(0).setDescription("AAA");
		modified.get(1).setComplete(true);
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(4, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/0/description", test.get("path").textValue());
		assertEquals("A", test.get("value").textValue());
		JsonNode replace = patch.get(1);
		assertEquals("replace", replace.get("op").textValue());
		assertEquals("/0/description", replace.get("path").textValue());
		assertEquals("AAA", replace.get("value").textValue());
		test = patch.get(2);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/1/complete", test.get("path").textValue());
		assertFalse(test.get("value").booleanValue());
		replace = patch.get(3);
		assertEquals("replace", replace.get("op").textValue());
		assertEquals("/1/complete", replace.get("path").textValue());
		assertTrue(replace.get("value").booleanValue());
	}
	
	@Test
	public void insertItemAtBeginningOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(0, new Todo(0L, "Z", false));
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(1, patch.size());
		JsonNode add = patch.get(0);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/0", add.get("path").textValue());
		JsonNode value = add.get("value");
		assertEquals(0L, value.get("id").longValue());
		assertEquals("Z", value.get("description").textValue());
		assertFalse(value.get("complete").booleanValue());
	}

	@Test
	public void insertTwoItemsAtBeginningOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(0, new Todo(25L, "Y", false));
		modified.add(0, new Todo(26L, "Z", true));
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode add = patch.get(0);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/0", add.get("path").textValue());
		JsonNode value = add.get("value");
		assertEquals(26L, value.get("id").longValue());
		assertEquals("Z", value.get("description").textValue());
		assertTrue(value.get("complete").booleanValue());
		add = patch.get(1);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/1", add.get("path").textValue());
		value = add.get("value");
		assertEquals(25L, value.get("id").longValue());
		assertEquals("Y", value.get("description").textValue());
		assertFalse(value.get("complete").booleanValue());
	}

	@Test
	public void insertItemAtMiddleOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(2, new Todo(0L, "Z", false));
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(1, patch.size());
		JsonNode add = patch.get(0);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/2", add.get("path").textValue());		
		JsonNode value = add.get("value");
		assertEquals(0L, value.get("id").longValue());
		assertEquals("Z", value.get("description").textValue());
		assertFalse(value.get("complete").booleanValue());

	}
	
	@Test
	public void insertTwoItemsAtMiddleOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(2, new Todo(25L, "Y", false));
		modified.add(2, new Todo(26L, "Z", true));
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode add = patch.get(0);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/2", add.get("path").textValue());
		JsonNode value = add.get("value");
		assertEquals(26L, value.get("id").longValue());
		assertEquals("Z", value.get("description").textValue());
		assertTrue(value.get("complete").booleanValue());
		add = patch.get(1);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/3", add.get("path").textValue());
		value = add.get("value");
		assertEquals(25L, value.get("id").longValue());
		assertEquals("Y", value.get("description").textValue());
		assertFalse(value.get("complete").booleanValue());
	}
	
	public void insertItemAtEndOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(3, new Todo(0L, "Z", false));
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(1, patch.size());
		JsonNode add = patch.get(0);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/3", add.get("path").textValue());
		assertEquals("{\"id\":0,\"description\":\"Z\",\"complete\":false}", add.get("value").textValue());
	}
	
	@Test
	public void insertTwoItemsAtEndOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(3, new Todo(25L, "Y", false));
		modified.add(4, new Todo(26L, "Z", true));
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode add = patch.get(0);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/3", add.get("path").textValue());
		JsonNode value = add.get("value");
		assertEquals(25L, value.get("id").longValue());
		assertEquals("Y", value.get("description").textValue());
		assertFalse(value.get("complete").booleanValue());
		add = patch.get(1);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/4", add.get("path").textValue());
		value = add.get("value");
		assertEquals(26L, value.get("id").longValue());
		assertEquals("Z", value.get("description").textValue());
		assertTrue(value.get("complete").booleanValue());
	}

	@Test
	public void insertItemsAtBeginningAndEndOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(0, new Todo(25L, "Y", false));
		modified.add(4, new Todo(26L, "Z", true));
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode add = patch.get(0);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/0", add.get("path").textValue());
		JsonNode value = add.get("value");
		assertEquals(25L, value.get("id").longValue());
		assertEquals("Y", value.get("description").textValue());
		assertFalse(value.get("complete").booleanValue());
		add = patch.get(1);
		assertEquals("add", add.get("op").textValue());
		assertEquals("/4", add.get("path").textValue());
		value = add.get("value");
		assertEquals(26L, value.get("id").longValue());
		assertEquals("Z", value.get("description").textValue());
		assertTrue(value.get("complete").booleanValue());
	}
	
	@Test
	public void removeItemFromBeginningOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.remove(0);
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/0", test.get("path").textValue());
		JsonNode value = test.get("value");
		assertEquals(1L, value.get("id").longValue());
		assertEquals("A", value.get("description").textValue());
		assertFalse(value.get("complete").booleanValue());
		JsonNode remove = patch.get(1);
		assertEquals("remove", remove.get("op").textValue());
		assertEquals("/0", remove.get("path").textValue());
	}

	@Test
	public void removeItemFromMiddleOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.remove(1);
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/1", test.get("path").textValue());
		JsonNode value = test.get("value");
		assertEquals(2L, value.get("id").longValue());
		assertEquals("B", value.get("description").textValue());
		assertFalse(value.get("complete").booleanValue());
		JsonNode remove = patch.get(1);
		assertEquals("remove", remove.get("op").textValue());
		assertEquals("/1", remove.get("path").textValue());
	}
	
	@Test
	public void removeItemFromEndOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.remove(2);
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(2, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/2", test.get("path").textValue());
		JsonNode value = test.get("value");
		assertEquals(3L, value.get("id").longValue());
		assertEquals("C", value.get("description").textValue());
		assertFalse(value.get("complete").booleanValue());
		JsonNode remove = patch.get(1);
		assertEquals("remove", remove.get("op").textValue());
		assertEquals("/2", remove.get("path").textValue());
	}
	
	@Test
	public void removeAllItemsFromList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.remove(0);
		modified.remove(0);
		modified.remove(0);
		
		JsonNode diff = new JsonDiff().diff(original, modified);
		assertTrue(diff.isArray());
		ArrayNode patch = (ArrayNode) diff;
		assertEquals(6, patch.size());
		JsonNode test = patch.get(0);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/0", test.get("path").textValue());
		JsonNode valueNode = test.get("value");
		assertEquals(1L, valueNode.get("id").longValue());
		assertEquals("A", valueNode.get("description").textValue());
		assertFalse(valueNode.get("complete").booleanValue());
		JsonNode remove = patch.get(1);
		assertEquals("remove", remove.get("op").textValue());
		assertEquals("/0", remove.get("path").textValue());
		test = patch.get(2);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/0", test.get("path").textValue());
		valueNode = test.get("value");
		assertEquals(2L, valueNode.get("id").longValue());
		assertEquals("B", valueNode.get("description").textValue());
		assertFalse(valueNode.get("complete").booleanValue());
		remove = patch.get(3);
		assertEquals("remove", remove.get("op").textValue());
		assertEquals("/0", remove.get("path").textValue());
		test = patch.get(4);
		assertEquals("test", test.get("op").textValue());
		assertEquals("/0", test.get("path").textValue());
		valueNode = test.get("value");
		assertEquals(3L, valueNode.get("id").longValue());
		assertEquals("C", valueNode.get("description").textValue());
		assertFalse(valueNode.get("complete").booleanValue());
		remove = patch.get(5);
		assertEquals("remove", remove.get("op").textValue());
		assertEquals("/0", remove.get("path").textValue());
	}

	private List<Todo> buildTodoList() {
		List<Todo> original = new ArrayList<Todo>();
		original.add(new Todo(1L, "A", false));
		original.add(new Todo(2L, "B", false));
		original.add(new Todo(3L, "C", false));
		return original;
	}
	
}
