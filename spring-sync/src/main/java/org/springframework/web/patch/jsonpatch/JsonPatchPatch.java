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

import org.springframework.web.patch.diffsync.Patch;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * JSON Patch implementation of {@link Patch}
 * @author Craig Walls
 */
public class JsonPatchPatch implements Patch {

	private JsonPatch patch;
	
	public JsonPatchPatch(JsonPatch patch) {
		this.patch = patch;
	}
	
	public static Patch fromJsonNode(JsonNode jsonNode) {
		return new JsonPatchPatch(JsonPatch.fromJsonNode(jsonNode));
	}
	
	@Override
	public Object apply(Object in) {
		return patch.apply(in);
	}
	
	@Override
	public boolean isEmpty() {
		return patch.size() == 0;
	}
	
}
