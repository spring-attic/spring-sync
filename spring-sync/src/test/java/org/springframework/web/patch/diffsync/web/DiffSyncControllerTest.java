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
package org.springframework.web.patch.diffsync.web;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.patch.Todo;
import org.springframework.web.patch.TodoRepository;
import org.springframework.web.patch.diffsync.EmbeddedDataSourceConfig;
import org.springframework.web.patch.diffsync.JpaPersistenceCallback;
import org.springframework.web.patch.diffsync.MapBasedShadowStore;
import org.springframework.web.patch.diffsync.PersistenceCallbackRegistry;
import org.springframework.web.patch.diffsync.ShadowStore;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=EmbeddedDataSourceConfig.class)
@Transactional
public class DiffSyncControllerTest {

	private static final String RESOURCE_PATH = "/todos";

	@Autowired
	private TodoRepository repository;
	
	private static final MediaType JSON_PATCH = new MediaType("application", "json-patch+json");
	
	@Test
	public void noChangesFromEitherSide() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);

		mvc.perform(
				patch(RESOURCE_PATH)
				.content("[]")
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH))
			.andExpect(status().isOk());
		
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
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content(resource("patch-change-single-status"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

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
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content(resource("patch-change-single-status-and-desc"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

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
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content(resource("patch-change-two-status-and-desc"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

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
	@Ignore
	public void patchAddsAnItem() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content(resource("patch-add-new-item"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[{\"op\":\"test\",\"path\":\"/3/id\"},{\"op\":\"add\",\"path\":\"/3/id\",\"value\":4}]"))
			.andExpect(content().contentType(JSON_PATCH));

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
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content(resource("patch-remove-item"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

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
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content(resource("patch-remove-two-items"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(1, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
	}


	@Test
	public void patchUpdatesStatusOnOneItemAndRemovesTwoOtherItems() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content(resource("patch-change-status-and-delete-two-items"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(1, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertTrue(all.get(0).isComplete());
	}

	@Test
	public void patchRemovesTwoOtherItemsAndUpdatesStatusOnAnother() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content(resource("patch-delete-twoitems-and-change-status-on-another"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(1, all.size());
		assertEquals(3L, all.get(0).getId().longValue());
		assertEquals("C", all.get(0).getDescription());
		assertTrue(all.get(0).isComplete());
	}

	@Test
	public void patchChangesItemStatusAndThenRemovesThatSameItem() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content(resource("patch-modify-then-remove-item"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

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

	private String resource(String name) throws IOException {
		ClassPathResource resource = new ClassPathResource("/org/springframework/web/patch/" + name + ".json");
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

	private MockMvc mockMvc(TodoRepository todoRepository) {
		ShadowStore shadowStore = new MapBasedShadowStore();
		
		PersistenceCallbackRegistry callbackRegistry = new PersistenceCallbackRegistry();
		callbackRegistry.addPersistenceCallback(new JpaPersistenceCallback<Todo>(todoRepository, Todo.class));
		
		DiffSyncController controller = new DiffSyncController(callbackRegistry, shadowStore);
		MockMvc mvc = standaloneSetup(controller)
				.setMessageConverters(new JsonPatchHttpMessageConverter())
				.build();
		return mvc;
	}
	
}
