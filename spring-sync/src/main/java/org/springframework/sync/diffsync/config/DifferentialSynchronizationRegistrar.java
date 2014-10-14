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
package org.springframework.sync.diffsync.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.sync.diffsync.MapBasedShadowStore;
import org.springframework.sync.diffsync.PersistenceCallbackRegistry;
import org.springframework.sync.diffsync.ShadowStore;
import org.springframework.sync.diffsync.web.DiffSyncController;
import org.springframework.util.Assert;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Configuration adapter for Differential Synchronization in Spring.
 * @author Craig Walls
 */
@Configuration
public class DifferentialSynchronizationRegistrar extends WebMvcConfigurerAdapter {

	private List<DiffSyncConfigurer> diffSyncConfigurers;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new MappingJackson2HttpMessageConverter());
	}

	@Autowired
	public void setDiffSyncConfigurers(List<DiffSyncConfigurer> diffSyncConfigurers) {
		Assert.notNull(diffSyncConfigurers, "At least one configuration class must implement DiffSyncConfigurer");
		Assert.notEmpty(diffSyncConfigurers, "At least one configuration class must implement DiffSyncConfigurer");
		this.diffSyncConfigurers = diffSyncConfigurers;
	}
		
	@Bean
	@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
	public ShadowStore shadowStore() {
		for (DiffSyncConfigurer diffSyncConfigurer : diffSyncConfigurers) {
			ShadowStore shadowStore = diffSyncConfigurer.getShadowStore();
			if (shadowStore != null) {
				return shadowStore;
			}
		}
		return new MapBasedShadowStore();
	}
	
	@Bean
	public PersistenceCallbackRegistry persistenceCallbackRegistry() {
		PersistenceCallbackRegistry registry = new PersistenceCallbackRegistry();
		for (DiffSyncConfigurer diffSyncConfigurer : diffSyncConfigurers) {
			diffSyncConfigurer.addPersistenceCallbacks(registry);
		}
		return registry;
	}
	
	@Bean
	public DiffSyncController diffSyncController(PersistenceCallbackRegistry callbackRegistry, ShadowStore shadowStore) {
		return new DiffSyncController(callbackRegistry, shadowStore);
	}

}
