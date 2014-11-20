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
package org.springframework.sync.diffsync.web;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

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
import org.springframework.sync.Todo;
import org.springframework.sync.TodoRepository;
import org.springframework.sync.diffsync.EmbeddedDataSourceConfig;
import org.springframework.sync.diffsync.PersistenceCallbackRegistry;
import org.springframework.sync.diffsync.ShadowStore;
import org.springframework.sync.diffsync.shadowstore.MapBasedShadowStore;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=EmbeddedDataSourceConfig.class)
@Transactional
public class DiffSyncControllerTest {

	private static final String RESOURCE_PATH = "/todos";

	@Autowired
	private TodoRepository repository;
	
	private static final MediaType JSON_PATCH = new MediaType("application", "json-patch+json");
	
	//
	// entity patching
	//
	
	@Test
	public void patchSendsEntityStatusChange() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH + "/2")
				.content(resource("patch-change-entity-status"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(3, all.size());
		assertEquals(new Todo(1L, "A", false), all.get(0));
		assertEquals(new Todo(2L, "B", true), all.get(1));
		assertEquals(new Todo(3L, "C", false), all.get(2));
	}

	@Test
	public void patchSendsEntityDescriptionChange() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH + "/2")
				.content(resource("patch-change-entity-description"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(3, all.size());
		assertEquals(new Todo(1L, "A", false), all.get(0));
		assertEquals(new Todo(2L, "BBB", false), all.get(1));
		assertEquals(new Todo(3L, "C", false), all.get(2));
	}

	@Test
	public void patchSendsEntityIdChange() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch(RESOURCE_PATH + "/2")
				.content(resource("patch-change-entity-id"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(4, all.size());
		assertEquals(new Todo(1L, "A", false), all.get(0));
		assertEquals(new Todo(2L, "B", false), all.get(1));
		assertEquals(new Todo(3L, "C", false), all.get(2));
		assertEquals(new Todo(4L, "B", false), all.get(3)); // This is odd behavior, but correct in the context of the backing database.
	}

	//
	// list patching
	//
	
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
		assertEquals(all.get(0), new Todo(1L, "A", false));
		assertEquals(all.get(1), new Todo(2L, "B", false));
		assertEquals(all.get(2), new Todo(3L, "C", false));
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
		assertEquals(all.get(0), new Todo(1L, "A", false));
		assertEquals(all.get(1), new Todo(2L, "B", true));
		assertEquals(all.get(2), new Todo(3L, "C", false));
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
		assertEquals(all.get(0), new Todo(1L, "A", false));
		assertEquals(all.get(1), new Todo(2L, "BBB", true));
		assertEquals(all.get(2), new Todo(3L, "C", false));
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
		assertEquals(all.get(0), new Todo(1L, "AAA", false));
		assertEquals(all.get(1), new Todo(2L, "B", true));
		assertEquals(all.get(2), new Todo(3L, "C", false));
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
		assertEquals(all.get(0), new Todo(1L, "A", false));
		assertEquals(all.get(1), new Todo(2L, "B", false));
		assertEquals(all.get(2), new Todo(3L, "C", false));
		assertEquals(all.get(2), new Todo(4L, "D", false));
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
		assertEquals(all.get(0), new Todo(1L, "A", false));
		assertEquals(all.get(1), new Todo(3L, "C", false));
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
		assertEquals(all.get(0), new Todo(1L, "A", false));
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
		assertEquals(all.get(0), new Todo(1L, "A", true));
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
		assertEquals(all.get(0), new Todo(3L, "C", true));
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
		assertEquals(all.get(0), new Todo(1L, "A", false));
		assertEquals(all.get(1), new Todo(3L, "C", false));
	}
	
	//
	// server-side changes
	//

	@Test
	@Ignore
	public void noChangesFromClientSide_itemDeletedFromServer() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);

		performNoOpRequestToSetupShadow(mvc);
		
		repository.delete(2L);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content("[]")
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(content().string(resource("patch-remove-item")))
			.andExpect(content().contentType(JSON_PATCH))
			.andExpect(status().isOk());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(2, all.size());
		assertEquals(new Todo(1L, "A", false), all.get(0));
		assertEquals(new Todo(3L, "C", false), all.get(1));
	}
	
	@Test
	@Ignore
	public void statusChangedOnClient_itemDeletedFromServer() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		performNoOpRequestToSetupShadow(mvc);
		
		repository.delete(2L);
		
		mvc.perform(
				patch(RESOURCE_PATH)
				.content(resource("patch-change-single-status"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(content().string(resource("patch-remove-completed-item")))
			.andExpect(content().contentType(JSON_PATCH))
			.andExpect(status().isOk());

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(2, all.size());
		assertEquals(new Todo(1L, "A", false), all.get(0));
		assertEquals(new Todo(3L, "C", false), all.get(1));
	}
	

	
	//
	// private helpers
	//

	private void performNoOpRequestToSetupShadow(MockMvc mvc) throws Exception {
		mvc.perform(
				patch(RESOURCE_PATH)
				.content("[]")
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH))
			.andExpect(status().isOk());
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

	private TodoRepository todoRepository() {
		return repository;
	}

	private MockMvc mockMvc(TodoRepository todoRepository) {
		ShadowStore shadowStore = new MapBasedShadowStore("x");
		
		PersistenceCallbackRegistry callbackRegistry = new PersistenceCallbackRegistry();
		callbackRegistry.addPersistenceCallback(new JpaPersistenceCallback<Todo>(todoRepository, Todo.class));
		
		DiffSyncController controller = new DiffSyncController(callbackRegistry, shadowStore);
		MockMvc mvc = standaloneSetup(controller)
				.setMessageConverters(new JsonPatchHttpMessageConverter())
				.build();
		return mvc;
	}
	
}
