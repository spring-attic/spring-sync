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

public class InverseTest extends TestCase {

	public void testInverseOnObjects() throws Exception {
		Todo original = new Todo(123L, "A", false);
		Todo modified = new Todo(124L, "B", true);
		Patch patch = Diff.diff(original, modified);
		Todo patched = patch.apply(original, Todo.class);
		assertEquals(modified, patched);
	}

	public void testInverseOnLists() throws Exception {
		List<Todo> original = new ArrayList<Todo>();
		original.add(new Todo(1L, "A", false));
		original.add(new Todo(2L, "B", false));
		original.add(new Todo(3L, "C", false));
		List<Todo> modified = new ArrayList<Todo>();
		modified.add(new Todo(111L, "A", false));
		modified.add(new Todo(2L, "BBB", false));
		modified.add(new Todo(3L, "C", true));
		Patch patch = Diff.diff(original, modified);
		List<Todo> patched = patch.apply(original, Todo.class);
		assertEquals(modified, patched);
	}

}
