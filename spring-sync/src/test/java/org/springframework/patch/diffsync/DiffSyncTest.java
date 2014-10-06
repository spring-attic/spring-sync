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
package org.springframework.patch.diffsync;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.patch.Patch;
import org.springframework.patch.Todo;
import org.springframework.patch.TodoRepository;
import org.springframework.patch.json.JsonPatchMaker;
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
	
	@Test
	public void noChangesFromEitherSide() throws Exception {
		Patch returnPatch = applyPatch("patch-empty");
		assertEquals(0, returnPatch.size());
		
		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(3, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(2L, all.get(1).getId().longValue());
		assertEquals("B", all.get(1).getDescription());
		assertFalse(all.get(1).isComplete());
		assertEquals(3L, all.get(2).getId().longValue());
		assertEquals("C", all.get(2).getDescription());
		assertFalse(all.get(2).isComplete());
	}

	@Test
	public void patchSendsSingleStatusChange() throws Exception {
		Patch returnPatch = applyPatch("patch-change-single-status");
		assertEquals(0, returnPatch.size());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(3, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(2L, all.get(1).getId().longValue());
		assertEquals("B", all.get(1).getDescription());
		assertTrue(all.get(1).isComplete());
		assertEquals(3L, all.get(2).getId().longValue());
		assertEquals("C", all.get(2).getDescription());
		assertFalse(all.get(2).isComplete());
	}

	@Test
	public void patchSendsAStatusChangeAndADescriptionChangeForSameItem() throws Exception {
		Patch returnPatch = applyPatch("patch-change-single-status-and-desc");
		assertEquals(0, returnPatch.size());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(3, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(2L, all.get(1).getId().longValue());
		assertEquals("BBB", all.get(1).getDescription());
		assertTrue(all.get(1).isComplete());
		assertEquals(3L, all.get(2).getId().longValue());
		assertEquals("C", all.get(2).getDescription());
		assertFalse(all.get(2).isComplete());
	}

	@Test
	public void patchSendsAStatusChangeAndADescriptionChangeForDifferentItems() throws Exception {
		Patch returnPatch = applyPatch("patch-change-two-status-and-desc");
		assertEquals(0, returnPatch.size());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(3, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("AAA", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(2L, all.get(1).getId().longValue());
		assertEquals("B", all.get(1).getDescription());
		assertTrue(all.get(1).isComplete());
		assertEquals(3L, all.get(2).getId().longValue());
		assertEquals("C", all.get(2).getDescription());
		assertFalse(all.get(2).isComplete());
	}

	@Test
	public void patchAddsAnItem() throws Exception {
		Patch returnPatch = applyPatch("patch-add-new-item");

//		Patch patchArray = returnPatch;
		assertEquals(2, returnPatch.size());
//		JsonNode testNode = patchArray.get(0);
//		assertEquals("test", testNode.get("op").textValue());
//		assertEquals("/3/id", testNode.get("path").textValue());
//		JsonNode addNode = patchArray.get(1);
//		assertEquals("add", addNode.get("op").textValue());
//		assertEquals("/3/id", addNode.get("path").textValue());
//		assertEquals(4L, addNode.get("value").longValue());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(4, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(2L, all.get(1).getId().longValue());
		assertEquals("B", all.get(1).getDescription());
		assertFalse(all.get(1).isComplete());
		assertEquals(3L, all.get(2).getId().longValue());
		assertEquals("C", all.get(2).getDescription());
		assertFalse(all.get(2).isComplete());
		assertEquals(4L, all.get(3).getId().longValue());
		assertEquals("D", all.get(3).getDescription());
		assertFalse(all.get(3).isComplete());
	}
	
	@Test
	public void patchRemovesAnItem() throws Exception {
		Patch returnPatch = applyPatch("patch-remove-item");
		assertEquals(0, returnPatch.size());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(2, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(3L, all.get(1).getId().longValue());
		assertEquals("C", all.get(1).getDescription());
		assertFalse(all.get(1).isComplete());
	}

	@Test
	public void patchRemovesTwoItems() throws Exception {
		Patch returnPatch = applyPatch("patch-remove-two-items");
		assertEquals(0, returnPatch.size());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(1, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
	}


	@Test
	public void patchUpdatesStatusOnOneItemAndRemovesTwoOtherItems() throws Exception {
		Patch returnPatch = applyPatch("patch-change-status-and-delete-two-items");
		assertEquals(0, returnPatch.size());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(1, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertTrue(all.get(0).isComplete());
	}

	@Test
	public void patchRemovesTwoOtherItemsAndUpdatesStatusOnAnother() throws Exception {
		Patch returnPatch = applyPatch("patch-delete-twoitems-and-change-status-on-another");
		assertEquals(0, returnPatch.size());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(1, all.size());
		assertEquals(3L, all.get(0).getId().longValue());
		assertEquals("C", all.get(0).getDescription());
		assertTrue(all.get(0).isComplete());
	}

	@Test
	public void patchChangesItemStatusAndThenRemovesThatSameItem() throws Exception {
		Patch returnPatch = applyPatch("patch-modify-then-remove-item");
		assertEquals(0, returnPatch.size());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(2, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(3L, all.get(1).getId().longValue());
		assertEquals("C", all.get(1).getDescription());
		assertFalse(all.get(1).isComplete());
	}

	
	//
	// private helpers
	//
	
	private Patch applyPatch(String patchResourceName) throws IOException, JsonProcessingException {
		Iterable<Todo> allTodos = todoRepository().findAll();
		Patch jsonPatch = readJsonPatchFromResource(patchResourceName);
		
		
		JpaPersistenceCallback<Todo> callback = new JpaPersistenceCallback<Todo>(todoRepository(), Todo.class);
		
		DiffSync<Todo> sync = new DiffSync<Todo>(jsonPatch, new MapBasedShadowStore(), callback);
		return sync.apply((List<Todo>) allTodos);
	}
	
	private Patch readJsonPatchFromResource(String resource) throws IOException, JsonProcessingException { 
		return new JsonPatchMaker().fromJsonNode(OBJECT_MAPPER.readTree(resource(resource)));
	}

	private String resource(String name) throws IOException {
		ClassPathResource resource = new ClassPathResource("/org/springframework/patch/" + name + ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		StringBuilder builder = new StringBuilder();
		while(reader.ready()) {
			builder.append(reader.readLine());
		}
		return builder.toString();
	}

	private TodoRepository todoRepository() {
		return repository;
	}
	
}
