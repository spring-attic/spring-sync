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

public class DiffTest extends TestCase {

	public void testNoChanges() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();

		Patch diff = Diff.diff(original, modified);
		assertEquals(0, diff.size());
	}

	public void testNullPropertyToNonNullProperty() throws Exception {
		Todo original = new Todo(null, "A", false);
		Todo modified = new Todo(1L, "A", false);
		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());

		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/id", op.getPath());
		assertNull(op.getValue());
	}

	public void testSingleBooleanPropertyChangeOnObject() throws Exception {
		Todo original = new Todo(1L, "A", false);
		Todo modified = new Todo(1L, "A", true);

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());

		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/complete", op.getPath());
		assertFalse((Boolean) op.getValue());
		op = ops.get(1);
		assertEquals("replace", op.getOp());
		assertEquals("/complete", op.getPath());
		assertTrue((Boolean) op.getValue());
	}

	public void testSingleStringPropertyChangeOnObject() throws Exception {
		Todo original = new Todo(1L, "A", false);
		Todo modified = new Todo(1L, "B", false);

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/description", op.getPath());
		assertEquals("A", op.getValue());
		op = ops.get(1);
		assertEquals("replace", op.getOp());
		assertEquals("/description", op.getPath());
		assertEquals("B", op.getValue());
	}

	public void testSingleNumericPropertyChangeOnObject() throws Exception {
		Todo original = new Todo(1L, "A", false);
		Todo modified = new Todo(2L, "A", false);

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/id", op.getPath());
		assertEquals(1L, op.getValue());
		op = ops.get(1);
		assertEquals("replace", op.getOp());
		assertEquals("/id", op.getPath());
		assertEquals(2L, op.getValue());
	}

	public void testChangeTwoPropertiesOnObject() throws Exception {
		Todo original = new Todo(1L, "A", false);
		Todo modified = new Todo(1L, "B", true);

		Patch diff = Diff.diff(original, modified);
		assertEquals(4, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/description", op.getPath());
		assertEquals("A", op.getValue());
		op = ops.get(1);
		assertEquals("replace", op.getOp());
		assertEquals("/description", op.getPath());
		assertEquals("B", op.getValue());
		op = ops.get(2);
		assertEquals("test", op.getOp());
		assertEquals("/complete", op.getPath());
		assertEquals(false, op.getValue());
		op = ops.get(3);
		assertEquals("replace", op.getOp());
		assertEquals("/complete", op.getPath());
		assertEquals(true, op.getValue());
	}

	public void testSingleBooleanPropertyChangeOnItemInList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.get(1).setComplete(true);

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/1/complete", op.getPath());
		assertEquals(false, op.getValue());
		op = ops.get(1);
		assertEquals("replace", op.getOp());
		assertEquals("/1/complete", op.getPath());
		assertEquals(true, op.getValue());
	}

	public void testSingleStringPropertyChangeOnItemInList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.get(1).setDescription("BBB");

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/1/description", op.getPath());
		assertEquals("B", op.getValue());
		op = ops.get(1);
		assertEquals("replace", op.getOp());
		assertEquals("/1/description", op.getPath());
		assertEquals("BBB", op.getValue());
	}

	public void testSingleMultiplePropertyChangeOnItemInList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.get(1).setComplete(true);
		modified.get(1).setDescription("BBB");

		Patch diff = Diff.diff(original, modified);
		assertEquals(4, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/1/description", op.getPath());
		assertEquals("B", op.getValue());
		op = ops.get(1);
		assertEquals("replace", op.getOp());
		assertEquals("/1/description", op.getPath());
		assertEquals("BBB", op.getValue());
		op = ops.get(2);
		assertEquals("test", op.getOp());
		assertEquals("/1/complete", op.getPath());
		assertEquals(false, op.getValue());
		op = ops.get(3);
		assertEquals("replace", op.getOp());
		assertEquals("/1/complete", op.getPath());
		assertEquals(true, op.getValue());
	}

	public void testPropertyChangeOnTwoItemsInList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.get(0).setDescription("AAA");
		modified.get(1).setComplete(true);

		Patch diff = Diff.diff(original, modified);
		assertEquals(4, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/0/description", op.getPath());
		assertEquals("A", op.getValue());
		op = ops.get(1);
		assertEquals("replace", op.getOp());
		assertEquals("/0/description", op.getPath());
		assertEquals("AAA", op.getValue());
		op = ops.get(2);
		assertEquals("test", op.getOp());
		assertEquals("/1/complete", op.getPath());
		assertEquals(false, op.getValue());
		op = ops.get(3);
		assertEquals("replace", op.getOp());
		assertEquals("/1/complete", op.getPath());
		assertEquals(true, op.getValue());
	}

	public void testInsertItemAtBeginningOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(0, new Todo(0L, "Z", false));
		Patch diff = Diff.diff(original, modified);
		assertEquals(1, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("add", op.getOp());
		assertEquals("/0", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(0L, value.getId().longValue());
		assertEquals("Z", value.getDescription());
		assertEquals(false, value.isComplete());
	}

	public void testInsertTwoItemsAtBeginningOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(0, new Todo(25L, "Y", false));
		modified.add(0, new Todo(26L, "Z", true));

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("add", op.getOp());
		assertEquals("/0", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(26L, value.getId().longValue());
		assertEquals("Z", value.getDescription());
		assertEquals(true, value.isComplete());
		op = ops.get(1);
		assertEquals("add", op.getOp());
		assertEquals("/1", op.getPath());
		value = (Todo) op.getValue();
		assertEquals(25L, value.getId().longValue());
		assertEquals("Y", value.getDescription());
		assertEquals(false, value.isComplete());
	}

	public void testInsertItemAtMiddleOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(2, new Todo(0L, "Z", false));
		Patch diff = Diff.diff(original, modified);
		assertEquals(1, diff.size());

		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("add", op.getOp());
		assertEquals("/2", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(0L, value.getId().longValue());
		assertEquals("Z", value.getDescription());
		assertEquals(false, value.isComplete());
	}

	public void testInsertTwoItemsAtMiddleOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(2, new Todo(25L, "Y", false));
		modified.add(2, new Todo(26L, "Z", true));

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("add", op.getOp());
		assertEquals("/2", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(26L, value.getId().longValue());
		assertEquals("Z", value.getDescription());
		assertEquals(true, value.isComplete());
		op = ops.get(1);
		assertEquals("add", op.getOp());
		assertEquals("/3", op.getPath());
		value = (Todo) op.getValue();
		assertEquals(25L, value.getId().longValue());
		assertEquals("Y", value.getDescription());
		assertEquals(false, value.isComplete());
	}

	public void testInsertItemAtEndOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(3, new Todo(0L, "Z", false));
		Patch diff = Diff.diff(original, modified);
		assertEquals(1, diff.size());

		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("add", op.getOp());
		assertEquals("/3", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(0L, value.getId().longValue());
		assertEquals("Z", value.getDescription());
		assertEquals(false, value.isComplete());
	}

	public void testInsertTwoItemsAtEndOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(3, new Todo(25L, "Y", false));
		modified.add(4, new Todo(26L, "Z", true));

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("add", op.getOp());
		assertEquals("/3", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(25L, value.getId().longValue());
		assertEquals("Y", value.getDescription());
		assertEquals(false, value.isComplete());
		op = ops.get(1);
		assertEquals("add", op.getOp());
		assertEquals("/4", op.getPath());
		value = (Todo) op.getValue();
		assertEquals(26L, value.getId().longValue());
		assertEquals("Z", value.getDescription());
		assertEquals(true, value.isComplete());
	}

	public void testInsertItemsAtBeginningAndEndOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.add(0, new Todo(25L, "Y", false));
		modified.add(4, new Todo(26L, "Z", true));

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("add", op.getOp());
		assertEquals("/0", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(25L, value.getId().longValue());
		assertEquals("Y", value.getDescription());
		assertEquals(false, value.isComplete());
		op = ops.get(1);
		assertEquals("add", op.getOp());
		assertEquals("/4", op.getPath());
		value = (Todo) op.getValue();
		assertEquals(26L, value.getId().longValue());
		assertEquals("Z", value.getDescription());
		assertEquals(true, value.isComplete());
	}

	public void testRemoveItemFromBeginningOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.remove(0);

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());

		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/0", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(1L, value.getId().longValue());
		assertEquals("A", value.getDescription());
		assertEquals(false, value.isComplete());
		op = ops.get(1);
		assertEquals("remove", op.getOp());
		assertEquals("/0", op.getPath());
	}

	public void testRemoveItemFromMiddleOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.remove(1);

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/1", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(2L, value.getId().longValue());
		assertEquals("B", value.getDescription());
		assertEquals(false, value.isComplete());
		op = ops.get(1);
		assertEquals("remove", op.getOp());
		assertEquals("/1", op.getPath());
	}

	public void testRemoveItemFromEndOfList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.remove(2);

		Patch diff = Diff.diff(original, modified);
		assertEquals(2, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/2", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(3L, value.getId().longValue());
		assertEquals("C", value.getDescription());
		assertEquals(false, value.isComplete());
		op = ops.get(1);
		assertEquals("remove", op.getOp());
		assertEquals("/2", op.getPath());
	}

	public void testRemoveAllItemsFromList() throws Exception {
		List<Todo> original = buildTodoList();
		List<Todo> modified = buildTodoList();
		modified.remove(0);
		modified.remove(0);
		modified.remove(0);

		Patch diff = Diff.diff(original, modified);
		assertEquals(6, diff.size());
		List<PatchOperation> ops = diff.getOperations();
		PatchOperation op = ops.get(0);
		assertEquals("test", op.getOp());
		assertEquals("/0", op.getPath());
		Todo value = (Todo) op.getValue();
		assertEquals(1L, value.getId().longValue());
		assertEquals("A", value.getDescription());
		assertEquals(false, value.isComplete());
		op = ops.get(1);
		assertEquals("remove", op.getOp());
		assertEquals("/0", op.getPath());
		op = ops.get(2);
		assertEquals("test", op.getOp());
		assertEquals("/0", op.getPath());
		value = (Todo) op.getValue();
		assertEquals(2L, value.getId().longValue());
		assertEquals("B", value.getDescription());
		assertEquals(false, value.isComplete());
		op = ops.get(3);
		assertEquals("remove", op.getOp());
		assertEquals("/0", op.getPath());
		op = ops.get(4);
		assertEquals("test", op.getOp());
		assertEquals("/0", op.getPath());
		value = (Todo) op.getValue();
		assertEquals(3L, value.getId().longValue());
		assertEquals("C", value.getDescription());
		assertEquals(false, value.isComplete());
		op = ops.get(5);
		assertEquals("remove", op.getOp());
		assertEquals("/0", op.getPath());
	}

	private List<Todo> buildTodoList() {
		List<Todo> original = new ArrayList<Todo>();
		original.add(new Todo(1L, "A", false));
		original.add(new Todo(2L, "B", false));
		original.add(new Todo(3L, "C", false));
		return original;
	}

}
