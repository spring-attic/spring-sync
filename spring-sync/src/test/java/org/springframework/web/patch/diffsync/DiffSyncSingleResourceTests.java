package org.springframework.web.patch.diffsync;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.patch.Todo;
import org.springframework.web.patch.TodoRepository;
import org.springframework.web.patch.patch.JsonPatchMaker;
import org.springframework.web.patch.patch.Patch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=EmbeddedDataSourceConfig.class)
@Transactional
public class DiffSyncSingleResourceTests {

	@Autowired
	private TodoRepository repository;
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	public void noChange() throws Exception {
		JsonNode returnPatch = applyPatch(1L, "patch-empty");
		assertTrue(returnPatch instanceof ArrayNode);
		assertEquals(0, ((ArrayNode) returnPatch).size());

		Todo todo = repository.findOne(1L);
		assertEquals(1L, todo.getId().longValue());
		assertEquals("A", todo.getDescription());
		assertFalse(todo.isComplete());
	}

	@Test
	public void statusChange() throws Exception {
		JsonNode returnPatch = applyPatch(1L, "single-change-status");
		assertTrue(returnPatch instanceof ArrayNode);
		assertEquals(0, ((ArrayNode) returnPatch).size());
		
		Todo todo = repository.findOne(1L);
		assertEquals(1L, todo.getId().longValue());
		assertEquals("A", todo.getDescription());
		assertTrue(todo.isComplete());
	}

	@Test
	public void statusAndDescriptionChange() throws Exception {
		JsonNode returnPatch = applyPatch(1L, "single-change-status-and-desc");
		assertTrue(returnPatch instanceof ArrayNode);
		assertEquals(0, ((ArrayNode) returnPatch).size());
		
		Todo todo = repository.findOne(1L);
		assertEquals(1L, todo.getId().longValue());
		assertEquals("BBB", todo.getDescription());
		assertTrue(todo.isComplete());
	}
		
	//
	// private helpers
	//
	
	private JsonNode applyPatch(Long id, String patchResourceName) throws IOException, JsonProcessingException {
		Todo todo = todoRepository().findOne(id);
		Patch jsonPatch = readJsonPatchFromResource(patchResourceName);

		JpaPersistenceCallback<Todo> callback = new JpaPersistenceCallback<Todo>(todoRepository(), Todo.class);

		DiffSync<Todo> sync = new DiffSync<Todo>(jsonPatch, new MapBasedShadowStore(), callback);
		return sync.apply(todo);
	}
	
	private Patch readJsonPatchFromResource(String resource) throws IOException, JsonProcessingException { 
		return new JsonPatchMaker().fromJsonNode(OBJECT_MAPPER.readTree(resource(resource)));
	}

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
	
}
