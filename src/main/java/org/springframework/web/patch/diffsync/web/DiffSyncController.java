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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.patch.diffsync.DiffSync;
import org.springframework.web.patch.diffsync.PersistenceCallback;
import org.springframework.web.patch.diffsync.PersistenceCallbackRegistry;
import org.springframework.web.patch.diffsync.ShadowStore;
import org.springframework.web.patch.jsonpatch.JsonPatch;
import org.springframework.web.patch.jsonpatch.JsonPatchException;

import com.fasterxml.jackson.databind.JsonNode;

@Controller
public class DiffSyncController {
	
	private ShadowStore shadowStore;

	private PersistenceCallbackRegistry callbackRegistry;

	@Autowired
	public DiffSyncController(PersistenceCallbackRegistry callbackRegistry, ShadowStore shadowStore) {
		this.callbackRegistry = callbackRegistry;
		this.shadowStore = shadowStore;
	}

	@RequestMapping(
			value="/sync/{resource}",  // TODO : Shouldn't have to hard-code "/sync" prefix. Should be configurable or non-existent.
			method=RequestMethod.PATCH, 
			consumes={"application/json", "application/json-patch+json"}, 
			produces={"application/json", "application/json-patch+json"})
	public ResponseEntity<JsonNode> patch(@PathVariable("resource") String resource, JsonPatch jsonPatch) throws JsonPatchException {
		PersistenceCallback<?> persistenceCallback = callbackRegistry.findPersistenceCallback(resource);
		
		List<?> items = (List<?>) persistenceCallback.findAll();
		
		DiffSync<Object> sync = new DiffSync(jsonPatch, shadowStore, persistenceCallback);
		JsonNode returnPatch = sync.apply(items);

		// return returnPatch
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json-patch+json"));
		ResponseEntity<JsonNode> responseEntity = new ResponseEntity<JsonNode>(returnPatch, headers, HttpStatus.OK);
		
		return responseEntity;
	}

}
