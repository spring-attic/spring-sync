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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.web.patch.Todo;

public class AddOperationTest {

	
	@Test
	public void addBooleanPropertyValue() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		AddOperation add = new AddOperation("/1/complete", "true");
		add.perform(todos);
		
		assertTrue(todos.get(1).isComplete());
	}
	
	
	@Test
	public void addItemToList() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		AddOperation add = new AddOperation("/1", "{\"description\":\"D\",\"complete\":true}");
		add.perform(todos);
		
		assertEquals(4, todos.size());
	}
	
}
