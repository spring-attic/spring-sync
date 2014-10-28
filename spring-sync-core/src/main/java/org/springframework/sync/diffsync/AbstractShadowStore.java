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

public abstract class AbstractShadowStore implements ShadowStore {

	private String nodeId;

	/**
	 * Shadow store constructor
	 * @param remoteNodeId the unique id of the node that this shadow store is being created for.
	 */
	public AbstractShadowStore(String remoteNodeId) {
		this.nodeId = remoteNodeId;
	}
	
	/**
	 * Produces a node-specific key by prefixing the key with the remote node ID.
	 * @param key the resource key
	 * @return a node-specific key
	 */
	protected String getNodeSpecificKey(String key) {
		return nodeId + ":" + key;
	}

	
}
