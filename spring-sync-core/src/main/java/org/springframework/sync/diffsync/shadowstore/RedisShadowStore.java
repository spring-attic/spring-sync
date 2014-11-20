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
package org.springframework.sync.diffsync.shadowstore;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.sync.diffsync.AbstractShadowStore;
import org.springframework.sync.diffsync.Shadow;
import org.springframework.sync.diffsync.ShadowStore;

/**
 * {@link ShadowStore} implementation that stores shadows in Redis, via an injected {@link RedisTemplate}.
 * 
 * @author Craig Walls
 */
public class RedisShadowStore extends AbstractShadowStore implements DisposableBean {

	private RedisOperations<String, Shadow<?>> redisTemplate;
	
	private List<String> keys = new ArrayList<String>();

	/**
	 * Constructs a Redis-based {@link ShadowStore}.
	 * @param remoteNodeId the unique id of the node that this shadow store is being created for.
	 * @param redisTemplate a {@link RedisOperations} that will be used to store shadow copies.
	 */
	public RedisShadowStore(String remoteNodeId, RedisOperations<String, Shadow<?>> redisTemplate) {
		super(remoteNodeId);
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void putShadow(String key, Shadow<?> shadow) {
		String nodeKey = getNodeSpecificKey(key);
		redisTemplate.opsForValue().set(nodeKey, shadow);
		keys.add(nodeKey);
	}

	@Override
	public Shadow<?> getShadow(String key) {
		return redisTemplate.opsForValue().get(getNodeSpecificKey(key));
	}

	@Override
	public void destroy() throws Exception {
		redisTemplate.delete(keys);
	}

}
