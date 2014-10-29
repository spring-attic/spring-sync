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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class MoveOperationTest extends TestCase {

	public void testMoveBooleanPropertyValue() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));

		try {
			MoveOperation move = new MoveOperation("/1/complete", "/0/complete");
			move.perform(todos, Todo.class);
			fail();
		}
		catch (PatchException e) {
			assertEquals("Path '/0/complete' is not nullable.", e.getMessage());
		}
		assertFalse(todos.get(1).isComplete());
	}

	public void testMoveStringPropertyValue() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));

		MoveOperation move = new MoveOperation("/1/description", "/0/description");
		move.perform(todos, Todo.class);

		assertEquals("A", todos.get(1).getDescription());
	}

	public void testMoveBooleanPropertyValueIntoStringProperty() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));

		try {
			MoveOperation move = new MoveOperation("/1/description", "/0/complete");
			move.perform(todos, Todo.class);
			fail();
		}
		catch (PatchException e) {
			assertEquals("Path '/0/complete' is not nullable.", e.getMessage());
		}
		assertEquals("B", todos.get(1).getDescription());
	}

	//
	// NOTE: Moving an item about in a list probably has zero effect, as the order of the
	// list is
	// usually determined by the DB query that produced the list. Moving things around in
	// a
	// java.util.List and then saving those items really means nothing to the DB, as the
	// properties that determined the original order are still the same and will result in
	// the same order when the objects are queries again.
	//

	public void testMoveListElementToBeginningOfList() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", true));
		todos.add(new Todo(3L, "C", false));

		MoveOperation move = new MoveOperation("/0", "/1");
		move.perform(todos, Todo.class);

		assertEquals(3, todos.size());
		assertEquals(2L, todos.get(0).getId().longValue());
		assertEquals("B", todos.get(0).getDescription());
		assertTrue(todos.get(0).isComplete());
	}

	public void testMoveListElementToMiddleOfList() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));

		MoveOperation move = new MoveOperation("/2", "/0");
		move.perform(todos, Todo.class);

		assertEquals(3, todos.size());
		assertEquals(1L, todos.get(2).getId().longValue());
		assertEquals("A", todos.get(2).getDescription());
		assertTrue(todos.get(2).isComplete());
	}

	public void testMoveListElementToEndOfList_usingIndex() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));

		MoveOperation move = new MoveOperation("/2", "/0");
		move.perform(todos, Todo.class);

		assertEquals(3, todos.size());
		assertEquals(1L, todos.get(2).getId().longValue());
		assertEquals("A", todos.get(2).getDescription());
		assertTrue(todos.get(2).isComplete());
	}

	public void testMoveListElementToBeginningOfList_usingTilde() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(3L, "C", false));
		todos.add(new Todo(4L, "E", false));
		todos.add(new Todo(2L, "G", false));

		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(1L, "A", true));
		expected.add(new Todo(2L, "G", false));
		expected.add(new Todo(3L, "C", false));
		expected.add(new Todo(4L, "E", false));

		MoveOperation move = new MoveOperation("/1", "/~");
		move.perform(todos, Todo.class);
		assertEquals(expected, todos);
	}

	public void testMoveListElementToEndOfList_usingTilde() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "G", false));
		todos.add(new Todo(3L, "C", false));
		todos.add(new Todo(4L, "E", false));

		List<Todo> expected = new ArrayList<Todo>();
		expected.add(new Todo(1L, "A", true));
		expected.add(new Todo(3L, "C", false));
		expected.add(new Todo(4L, "E", false));
		expected.add(new Todo(2L, "G", false));

		MoveOperation move = new MoveOperation("/~", "/1");
		move.perform(todos, Todo.class);
		assertEquals(expected, todos);
	}

}
