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
package org.springframework.sync.diffsync;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.sync.Patch;
import org.springframework.sync.PatchException;
import org.springframework.sync.Todo;
import org.springframework.sync.TodoRepository;
import org.springframework.sync.json.JsonPatchMaker;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=EmbeddedDataSourceConfig.class)
@Transactional
public class DiffSyncTest {

	@Autowired
	private TodoRepository repository;
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	@After
	public void cleanup() {
		repository.deleteAll();
	}

	//
	// Apply patches - lists
	//

	@Test
	public void patchList_emptyPatch() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-empty");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(patch, todos);
		assertEquals(patched, getTodoList());
		// original remains unchanged
		assertEquals(todos, getTodoList());
	}
	
	@Test
	public void patchList_addNewItem() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-add-new-item");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(patch, todos);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(4, patched.size());
		assertEquals(todos.get(0), patched.get(0));
		assertEquals(todos.get(1), patched.get(1));
		assertEquals(todos.get(2), patched.get(2));
		assertEquals(new Todo(null, "D", false), patched.get(3));
	}

	@Test
	public void patchList_changeSingleEntityStatusAndDescription() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-change-single-status-and-desc");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(patch, todos);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(3, patched.size());
		assertEquals(todos.get(0), patched.get(0));
		assertEquals(new Todo(2L, "BBB", true), patched.get(1));
		assertEquals(todos.get(2), patched.get(2));
	}

	@Test
	public void patchList_changeSingleEntityStatus() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-change-single-status");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(patch, todos);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(3, patched.size());
		assertEquals(todos.get(0), patched.get(0));
		assertEquals(new Todo(2L, "B", true), patched.get(1));
		assertEquals(todos.get(2), patched.get(2));
	}
	
	@Test
	public void patchList_changeStatusAndDeleteTwoItems() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-change-status-and-delete-two-items");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(patch, todos);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(1, patched.size());
		assertEquals(new Todo(1L, "A", true), patched.get(0));
	}

	@Test
	public void patchList_changeTwoStatusAndDescription() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-change-two-status-and-desc");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(patch, todos);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(3, patched.size());
		assertEquals(new Todo(1L, "AAA", false), patched.get(0));
		assertEquals(new Todo(2L, "B", true), patched.get(1));
		assertEquals(new Todo(3L, "C", false), patched.get(2));
	}

	@Test
	public void patchList_deleteTwoItemsAndChangeStatusOnAnother() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-delete-twoitems-and-change-status-on-another");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(patch, todos);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(1, patched.size());
		assertEquals(new Todo(3L, "C", true), patched.get(0));
	}

	@Test
	public void patchList_patchFailingOperationFirst() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-failing-operation-first");

		List<Todo> todos = getTodoList();
		List<Todo> patched = null;
		try {
			patched = sync.apply(patch, todos);
			fail();
		} catch (PatchException e) {
			// original should remain unchanged
			assertEquals(todos, getTodoList());
			assertNull(patched);			
		}		
	}

	@Test
	public void patchList_patchFailingOperationInMiddle() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-failing-operation-in-middle");

		List<Todo> todos = getTodoList();
		List<Todo> patched = null;
		try {
			patched = sync.apply(patch, todos);
			fail();
		} catch (PatchException e) {
			// original should remain unchanged
			assertEquals(todos, getTodoList());
			assertNull(patched);			
		}		
	}

	@Test
	public void patchList_manySuccessfulOperations() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-many-successful-operations");

		List<Todo> todos = getBigTodoList();
		List<Todo> patched = sync.apply(patch, todos);

		// original should remain unchanged
		assertEquals(todos, getBigTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(6, patched.size());
		assertEquals(new Todo(1L, "A", true), patched.get(0));
		assertEquals(new Todo(2L, "B", true), patched.get(1));
		assertEquals(new Todo(3L, "C", false), patched.get(2));
		assertEquals(new Todo(4L, "C", false), patched.get(3));
		assertEquals(new Todo(1L, "A", true), patched.get(4));
		assertEquals(new Todo(5L, "E", false), patched.get(5));
	}

	@Test
	public void patchList_modifyThenRemoveItem() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-modify-then-remove-item");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(patch, todos);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(2, patched.size());
		assertEquals(new Todo(1L, "A", false), patched.get(0));
		assertEquals(new Todo(3L, "C", false), patched.get(1));
	}

	@Test
	public void patchList_removeItem() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-remove-item");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(patch, todos);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(2, patched.size());
		assertEquals(new Todo(1L, "A", false), patched.get(0));
		assertEquals(new Todo(3L, "C", false), patched.get(1));
	}
	
	@Test
	public void patchList_removeTwoItems() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-remove-two-items");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(patch, todos);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(1, patched.size());
		assertEquals(new Todo(1L, "A", false), patched.get(0));
	}
	
	//
	// Apply patches - single entity
	//

	@Test
	public void patchEntity_emptyPatch() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-empty");

		Todo todo = new Todo(1L, "A", false);
		Todo patched = sync.apply(patch, todo);
		assertEquals(1L, patched.getId().longValue());
		assertEquals("A", patched.getDescription());
		assertFalse(patched.isComplete());
		// original remains unchanged
		assertEquals(1L, todo.getId().longValue());
		assertEquals("A", todo.getDescription());
		assertFalse(todo.isComplete());
	}

	@Test
	public void patchEntity_booleanProperty() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("single-change-status");

		Todo todo = new Todo(1L, "A", false);
		Todo patched = sync.apply(patch, todo);
		assertEquals(1L, patched.getId().longValue());
		assertEquals("A", patched.getDescription());
		assertTrue(patched.isComplete());
		// original remains unchanged
		assertEquals(1L, todo.getId().longValue());
		assertEquals("A", todo.getDescription());
		assertFalse(todo.isComplete());

	}
	
	@Test
	public void patchEntity_stringProperty() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("single-change-description");

		Todo todo = new Todo(1L, "A", false);
		Todo patched = sync.apply(patch, todo);
		assertEquals(1L, patched.getId().longValue());
		assertEquals("AAA", patched.getDescription());
		assertFalse(patched.isComplete());
		// original remains unchanged
		assertEquals(1L, todo.getId().longValue());
		assertEquals("A", todo.getDescription());
		assertFalse(todo.isComplete());
	}

	@Test
	public void patchEntity_numericProperty() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("single-change-id");

		Todo todo = new Todo(1L, "A", false);
		Todo patched = sync.apply(patch, todo);
		assertEquals(123L, patched.getId().longValue());
		assertEquals("A", patched.getDescription());
		assertFalse(patched.isComplete());
		// original remains unchanged
		assertEquals(1L, todo.getId().longValue());
		assertEquals("A", todo.getDescription());
		assertFalse(todo.isComplete());
	}
	
	@Test
	public void patchEntity_stringAndBooleanProperties() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore(), Todo.class);
		Patch patch = readJsonPatchFromResource("single-change-status-and-desc");

		Todo todo = new Todo(1L, "A", false);
		Todo patched = sync.apply(patch, todo);
		assertEquals(1L, patched.getId().longValue());
		assertEquals("BBB", patched.getDescription());
		assertTrue(patched.isComplete());
		// original remains unchanged
		assertEquals(1L, todo.getId().longValue());
		assertEquals("A", todo.getDescription());
		assertFalse(todo.isComplete());		
	}

	
	//
	// private helpers
	//
	
	private List<Todo> getTodoList() {
		List<Todo> todos = new ArrayList<Todo>();
		
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		return todos;
	}

	private List<Todo> getBigTodoList() {
		List<Todo> todos = new ArrayList<Todo>();
		
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		todos.add(new Todo(4L, "D", false));
		todos.add(new Todo(5L, "E", false));
		todos.add(new Todo(6L, "F", false));
		
		return todos;
	}
	
	private Patch readJsonPatchFromResource(String resource) throws IOException, JsonProcessingException { 
		return new JsonPatchMaker().fromJsonNode(OBJECT_MAPPER.readTree(resource(resource)));
	}

	private String resource(String name) throws IOException {
		ClassPathResource resource = new ClassPathResource("/org/springframework/sync/" + name + ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		StringBuilder builder = new StringBuilder();
		while(reader.ready()) {
			builder.append(reader.readLine());
		}
		return builder.toString();
	}
	
}
