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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.springframework.sync.AddOperation;
import org.springframework.sync.MoveOperation;
import org.springframework.sync.Patch;
import org.springframework.sync.PatchException;
import org.springframework.sync.PatchOperation;
import org.springframework.sync.Person;
import org.springframework.sync.Todo;
import org.springframework.sync.TodoRepository;
import org.springframework.sync.json.JsonPatchPatchConverter;
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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-empty");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, patch);
		assertEquals(patched, getTodoList());
		// original remains unchanged
		assertEquals(todos, getTodoList());
	}
	
	@Test
	public void patchList_addNewItem() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-add-new-item");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, patch);

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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-change-single-status-and-desc");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, patch);

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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-change-single-status");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, patch);

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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-change-status-and-delete-two-items");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, patch);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(1, patched.size());
		assertEquals(new Todo(1L, "A", true), patched.get(0));
	}

	@Test
	public void patchList_changeTwoStatusAndDescription() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-change-two-status-and-desc");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, patch);

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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-delete-twoitems-and-change-status-on-another");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, patch);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(1, patched.size());
		assertEquals(new Todo(3L, "C", true), patched.get(0));
	}

	@Test
	public void patchList_patchFailingOperationFirst() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-failing-operation-first");

		List<Todo> todos = getTodoList();
		List<Todo> patched = null;
		try {
			patched = sync.apply(todos, patch);
			fail();
		} catch (PatchException e) {
			// original should remain unchanged
			assertEquals(todos, getTodoList());
			assertNull(patched);			
		}		
	}

	@Test
	public void patchList_patchFailingOperationInMiddle() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-failing-operation-in-middle");

		List<Todo> todos = getTodoList();
		List<Todo> patched = null;
		try {
			patched = sync.apply(todos, patch);
			fail();
		} catch (PatchException e) {
			// original should remain unchanged
			assertEquals(todos, getTodoList());
			assertNull(patched);			
		}		
	}

	@Test
	public void patchList_manySuccessfulOperations() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-many-successful-operations");

		List<Todo> todos = getBigTodoList();
		List<Todo> patched = sync.apply(todos, patch);

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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-modify-then-remove-item");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, patch);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(2, patched.size());
		assertEquals(new Todo(1L, "A", false), patched.get(0));
		assertEquals(new Todo(3L, "C", false), patched.get(1));
	}

	@Test
	public void patchList_removeItem() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-remove-item");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, patch);

		// original should remain unchanged
		assertEquals(todos, getTodoList());
		
		assertNotEquals(patched, todos);
		assertEquals(2, patched.size());
		assertEquals(new Todo(1L, "A", false), patched.get(0));
		assertEquals(new Todo(3L, "C", false), patched.get(1));
	}
	
	@Test
	public void patchList_removeTwoItems() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-remove-two-items");

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, patch);

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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-empty");

		Todo todo = new Todo(1L, "A", false);
		Todo patched = sync.apply(todo, patch);
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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("single-change-status");

		Todo todo = new Todo(1L, "A", false);
		Todo patched = sync.apply(todo, patch);
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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("single-change-description");

		Todo todo = new Todo(1L, "A", false);
		Todo patched = sync.apply(todo, patch);
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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("single-change-id");

		Todo todo = new Todo(1L, "A", false);
		Todo patched = sync.apply(todo, patch);
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
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("single-change-status-and-desc");

		Todo todo = new Todo(1L, "A", false);
		Todo patched = sync.apply(todo, patch);
		assertEquals(1L, patched.getId().longValue());
		assertEquals("BBB", patched.getDescription());
		assertTrue(patched.isComplete());
		// original remains unchanged
		assertEquals(1L, todo.getId().longValue());
		assertEquals("A", todo.getDescription());
		assertFalse(todo.isComplete());		
	}
	
	@Test
	public void patchEntity_moveProperty() throws Exception {
		DiffSync<Person> sync = new DiffSync<Person>(new MapBasedShadowStore("x"), Person.class);
		List<PatchOperation> ops = new ArrayList<PatchOperation>();
		ops.add(new MoveOperation("/firstName", "/lastName"));
		Patch patch = new Patch(ops);
		
		Person person = new Person("Edmund", "Blackadder");
		Person patched = sync.apply(person, patch);
		assertEquals("Blackadder", patched.getFirstName());
		assertNull(patched.getLastName());
	}

	
	//
	// Guaranteed Delivery - Normal operations scenario
	//
	@Test
	public void patchList_addNewItem_normal() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-add-new-item");
		VersionedPatch versionedPatch = new VersionedPatch(patch.getOperations(), 0, 0);

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, versionedPatch);
		VersionedPatch diff = sync.diff(patched);
		assertEquals(1, diff.getClientVersion()); // the server is acknowledge client version 1 (the client should be at that version by this time)
		assertEquals(0, diff.getServerVersion()); // the server created the patch against server version 0 (but it will be 1 after the patch is created)

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
	public void patchEntity_moveProperty_normal() throws Exception {
		DiffSync<Person> sync = new DiffSync<Person>(new MapBasedShadowStore("x"), Person.class);
		List<PatchOperation> ops = new ArrayList<PatchOperation>();
		ops.add(new MoveOperation("/firstName", "/lastName"));
		VersionedPatch vPatch1 = new VersionedPatch(ops, 0, 0);
		
		Person person = new Person("Edmund", "Blackadder");
		Person patched = sync.apply(person, vPatch1);
		VersionedPatch diff = sync.diff(patched);
		assertEquals(1, diff.getClientVersion()); // the server is acknowledge client version 1 (the client should be at that version by this time)
		assertEquals(0, diff.getServerVersion()); // the server created the patch against server version 0 (but it will be 1 after the patch is created)
		
		assertEquals("Blackadder", patched.getFirstName());
		assertNull(patched.getLastName());
	}

	
	//
	// Guaranteed Delivery - Duplicate packet scenario
	//
	@Test
	public void patchList_addNewItem_duplicate() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);
		Patch patch = readJsonPatchFromResource("patch-add-new-item");
		VersionedPatch versionedPatch = new VersionedPatch(patch.getOperations(), 0, 0);
		VersionedPatch versionedPatch2 = new VersionedPatch(patch.getOperations(), 0, 0);

		List<Todo> todos = getTodoList();
		List<Todo> patched = sync.apply(todos, versionedPatch, versionedPatch2);
		VersionedPatch diff = sync.diff(patched);
		assertEquals(1, diff.getClientVersion()); // the server is acknowledge client version 1 (the client should be at that version by this time)
		assertEquals(0, diff.getServerVersion()); // the server created the patch against server version 0 (but it will be 1 after the patch is created)

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
	public void patchEntity_moveProperty_duplicate() throws Exception {
		DiffSync<Person> sync = new DiffSync<Person>(new MapBasedShadowStore("x"), Person.class);
		List<PatchOperation> ops = new ArrayList<PatchOperation>();
		ops.add(new MoveOperation("/firstName", "/lastName"));
		VersionedPatch vPatch1 = new VersionedPatch(ops, 0, 0);
		VersionedPatch vPatch2 = new VersionedPatch(ops, 0, 0);
		
		Person person = new Person("Edmund", "Blackadder");
		Person patched = sync.apply(person, vPatch1, vPatch2);
		VersionedPatch diff = sync.diff(patched);
		assertEquals(1, diff.getClientVersion()); // the server is acknowledge client version 1 (the client should be at that version by this time)
		assertEquals(0, diff.getServerVersion()); // the server created the patch against server version 0 (but it will be 1 after the patch is created)

		assertEquals("Blackadder", patched.getFirstName());
		assertNull(patched.getLastName());
	}
	
	
	//
	// Guaranteed Delivery - Lost outbound packet scenario
	//
	// TODO: This is primarily a client-side case. By definition, the server never receives the patch.
	//       Therefore, there's nothing server-side to be tested.
	//       However, this case *does* apply to Spring Sync when used in an Android client.
	//       Therefore, tests for this scenario will need to be fleshed out.

	
	//
	// Guaranteed Delivery - Lost return packet scenario
	//
	@Test
	public void patchList_addNewItem_lostReturn() throws Exception {
		DiffSync<Todo> sync = new DiffSync<Todo>(new MapBasedShadowStore("x"), Todo.class);

		// Create the list resource
		List<Todo> todos = getTodoList();

		// Apply an initial patch to get the server shadow's client version bumped up.
		// Initially, the server shadow's server and client versions are both 0,
		// matching the incoming patch's versions, so the patch is applied normally.
		List<PatchOperation> ops1 = new ArrayList<PatchOperation>();
		ops1.add(new AddOperation("/~", new Todo(100L, "NEW ITEM 100", false)));
		VersionedPatch versionedPatch = new VersionedPatch(ops1, 0, 0);
		
		// At this point, the client sends the patch to the server, the client puts the patch in an outbound stack, 
		// the client increments its shadow client version to 1, and the server calls sync.apply() to apply the patch.
		List<Todo> patched = sync.apply(todos, versionedPatch);

		// After the patch is applied, the server shadow versions are
		//   - Primary shadow: serverVersion = 0, clientVersion = 1
		//   - Backup shadow : serverVersion = 0, clientVersion = 1
		
		// At this point, the server's shadow has client version 1 and server version 0
        // The server then copies its current shadow to backup shadow before performing a new diff against the shadow, bumping the server version to 1 *after* the diff is performed.
		// The backup shadow, having been taken before the new diff was created, still has server version 0.
		// Before it performs the diff, however, it copies its current shadow to backup shadow.
		// The diff was performed against the shadow whose client version 1 and server version 0, therefore the patch will have client version 1 and server version 0.
		VersionedPatch lostDiff = sync.diff(patched);
		
		// After the diff is applied, the server shadow's server version is incremented.
		//   - Primary shadow: serverVersion = 1, clientVersion = 1
		//   - Backup shadow : serverVersion = 0, clientVersion = 1
		
		// Verify that the patch has client version 1, server version 0
		assertEquals(1, lostDiff.getClientVersion()); 
		assertEquals(0, lostDiff.getServerVersion());

		// In the lost return packet scenario, the client never receives that return diff (lostDiff) or acknowledgement of the server having applied the first patch.
		// The client can only assume that the server never received it (although it did).
		// So it produces a new patch against its shadow (whose server version is still at 0 and client version is 1).
		// It then sends both patches to the server and the server attempts to apply them both.
		List<PatchOperation> ops2 = new ArrayList<PatchOperation>();
		ops2.add(new AddOperation("/~", new Todo(200L, "NEW ITEM 200", false)));
		VersionedPatch versionedPatch2 = new VersionedPatch(ops2, 0, 1);
		patched = sync.apply(patched, versionedPatch, versionedPatch2);

		// The first patch's server version is 0, which is less than the server shadow's server version of 1.
		// This indicates a lost packet scenario, meaning that the client never received or applied the
		// return patch from the previous cycle.
		// So the server resurrects the backup shadow into the primary shadow:
		//   - Primary shadow: serverVersion = 0, clientVersion = 1
		//   - Backup shadow : serverVersion = 0, clientVersion = 1
		// Then it tries to apply the first patch. Since the patch's client version is less than the shadow's client version, 
		// it ignores the patch as a duplicate (that was applied earlier)
		// Then it tries to apply the second patch. This patch's client version is the same as the shadow's client version, 
		// so it applies it as with normal operation.
		
		// After the applying the 2nd patch, the server shadow's server version is incremented.
		//   - Primary shadow: serverVersion = 0, clientVersion = 2
		//   - Backup shadow : serverVersion = 0, clientVersion = 2
		
		// Finally, the server performs a diff against the shadow (whose server version is 0 and whose client version is 2).
		// Therefore, the patch produced should have client version 2, server version 0.
		// After the diff, the server version will be 1, but there's no way to verify that, except to perform another patch.
		VersionedPatch diff = sync.diff(patched);
		assertEquals(2, diff.getClientVersion()); // the server is acknowledging client version 1 and 2 (the client should be at that version by this time)
		assertEquals(0, diff.getServerVersion()); // the server created the patch against server version 0 (but it will be 1 after the patch is created)

		// After the diff is applied, the server shadow's server version is incremented.
		//   - Primary shadow: serverVersion = 1, clientVersion = 2
		//   - Backup shadow : serverVersion = 0, clientVersion = 2

		
		// Now test that the resulting list is as expected.
		// The original should remain unchanged
		assertEquals(todos, getTodoList());
		
		// The patched resource should now contain 2 additional items, one from each patch sent.
		// It should *NOT* have two of the item that was added as part of the initial patch (the one that was sent twice).
		assertNotEquals(patched, todos);
		assertEquals(5, patched.size()); // Should only have added 2 new items. It shouldn't have added the first new item twice.
		assertEquals(todos.get(0), patched.get(0));
		assertEquals(todos.get(1), patched.get(1));
		assertEquals(todos.get(2), patched.get(2));
		assertEquals(new Todo(100L, "NEW ITEM 100", false), patched.get(3));
		assertEquals(new Todo(200L, "NEW ITEM 200", false), patched.get(4));
	}
	
	@Test
	public void patchEntity_moveProperty_lostReturnPacket() throws Exception {
		DiffSync<Person> sync = new DiffSync<Person>(new MapBasedShadowStore("x"), Person.class);
		
		Person person = new Person("Edmund", "Blackadder");

		List<PatchOperation> ops1 = new ArrayList<PatchOperation>();
		ops1.add(new MoveOperation("/firstName", "/lastName"));
		VersionedPatch vPatch1 = new VersionedPatch(ops1, 0, 0);
		Person patched = sync.apply(person, vPatch1);

		assertEquals("Blackadder", patched.getFirstName());
		assertNull(patched.getLastName());

		VersionedPatch lostDiff = sync.diff(patched);
		assertEquals(1, lostDiff.getClientVersion());
		assertEquals(0, lostDiff.getServerVersion());
		
		List<PatchOperation> ops2 = new ArrayList<PatchOperation>();
		ops2.add(new MoveOperation("/lastName", "/firstName"));
		VersionedPatch vPatch2 = new VersionedPatch(ops2, 0, 1);
		patched = sync.apply(patched, vPatch1, vPatch2);

		VersionedPatch diff = sync.diff(patched);
		assertEquals(2, diff.getClientVersion()); 
		assertEquals(0, diff.getServerVersion()); 

		assertNull(patched.getFirstName());
		assertEquals("Blackadder", patched.getLastName());
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
		return new JsonPatchPatchConverter().convert(OBJECT_MAPPER.readTree(resource(resource)));
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
