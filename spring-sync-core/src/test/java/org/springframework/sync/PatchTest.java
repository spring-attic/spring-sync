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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PatchTest {

	@Test
	public void replacePropertyOnEntityInListProperty() throws Exception {

		ArrayList<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		TodoList before = new TodoList();
		before.setTodos(todos);
		
		todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		todos.add(new Todo(4L, "D", false));
		TodoList after = new TodoList();
		after.setTodos(todos);
		
		Patch diff = Diff.diff(before, after);
		List<PatchOperation> operations = diff.getOperations();
		assertEquals(1, diff.size());
		assertEquals("add", operations.get(0).getOp());
		assertEquals("/todos/3", operations.get(0).getPath());
		assertEquals(new Todo(4L, "D", false), operations.get(0).getValue());
	}

}
