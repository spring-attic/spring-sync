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
package org.springframework.web.patch.diffsync;

/**
 * Strategy interface for maintaining shadow copies across requests.
 * 
 * @author Craig Walls
 */
public interface ShadowStore {
	
	/**
	 * Stores a shadow copy.
	 * @param key the key to store the shadow under
	 * @param shadow the shadow copy
	 */
	void putShadow(String key, Object shadow);
	
	/**
	 * Retrieves a shadow copy.
	 * @param key the key that the shadow is stored under
	 * @return the shadow copy
	 */
	Object getShadow(String key);

}
