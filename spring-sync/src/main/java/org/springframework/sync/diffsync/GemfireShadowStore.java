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
package org.springframework.sync.diffsync;

import org.springframework.data.gemfire.GemfireOperations;

/**
 * {@link ShadowStore} implementation that stores shadow copies in Pivotal GemFire.
 * 
 * @author Craig Walls
 */
public class GemfireShadowStore implements ShadowStore {

	private GemfireOperations gemfireTemplate;

	/**
	 * Constructs a GemFire-based {@link ShadowStore}.
	 * @param gemfireTemplate a {@link GemfireOperations} that will be used to store shadow copies.
	 */
	public GemfireShadowStore(GemfireOperations gemfireTemplate) {
		this.gemfireTemplate = gemfireTemplate;
	}
	
	@Override
	public void putShadow(String key, Object shadow) {
		gemfireTemplate.put(key, shadow);
	}

	@Override
	public Object getShadow(String key) {
		return gemfireTemplate.get(key);
	}

}
