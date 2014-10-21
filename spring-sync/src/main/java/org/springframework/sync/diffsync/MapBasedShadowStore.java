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

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link ShadowStore} that keeps shadows in an in-memory map.
 * Not recommended for production applications, as it isn't scalable in terms of the number of clients.
 * Consider {@link RedisShadowStore} or {@link GemfireShadowStore} instead.
 * @author Craig Walls
 */
public class MapBasedShadowStore extends AbstractShadowStore {

	private Map<String, Object> store = new HashMap<String, Object>();
	
	public MapBasedShadowStore(String remoteNodeId) {
		super(remoteNodeId);
	}
	
	@Override
	public void putShadow(String key, Object shadow) {
		store.put(getNodeSpecificKey(key), shadow);
	}

	@Override
	public Object getShadow(String key) {
		return store.get(getNodeSpecificKey(key));
	}

}
