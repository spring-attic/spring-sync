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
package org.springframework.sync.diffsync.data;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.gemfire.GemfireOperations;
import org.springframework.sync.diffsync.AbstractShadowStore;
import org.springframework.sync.diffsync.ShadowStore;

/**
 * {@link ShadowStore} implementation that stores shadow copies in Pivotal GemFire.
 * 
 * @author Craig Walls
 */
public class GemfireShadowStore extends AbstractShadowStore implements DisposableBean {

	private GemfireOperations gemfireTemplate;
	
	private List<String> keys = new ArrayList<String>();

	/**
	 * Constructs a GemFire-based {@link ShadowStore}.
	 * @param remoteNodeId the unique id of the node that this shadow store is being created for.
	 * @param gemfireTemplate a {@link GemfireOperations} that will be used to store shadow copies.
	 */
	public GemfireShadowStore(String remoteNodeId, GemfireOperations gemfireTemplate) {
		super(remoteNodeId);
		this.gemfireTemplate = gemfireTemplate;
	}
	
	@Override
	public void putShadow(String key, Object shadow) {
		String nodeKey = getNodeSpecificKey(key);
		gemfireTemplate.put(nodeKey, shadow);
		keys.add(nodeKey);
	}

	@Override
	public Object getShadow(String key) {
		return gemfireTemplate.get(getNodeSpecificKey(key));
	}

	@Override
	public void destroy() throws Exception {
		for (String key : keys) {
			gemfireTemplate.remove(key);
		}
	}
	
}
