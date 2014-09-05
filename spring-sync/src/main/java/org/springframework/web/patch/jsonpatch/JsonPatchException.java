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

/**
 * Exception thrown if an error occurs in the course of applying a JSON Patch.
 * 
 * @author Craig Walls
 */
public class JsonPatchException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public JsonPatchException(String message) {
		super(message);
	}
	
	public JsonPatchException(String message, Exception e) {
		super(message, e);
	}

}
