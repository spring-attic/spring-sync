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

import java.util.List;

import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Spring MVC method argument resolver that resolves a JSON request payload containing JSON Patch into a JsonPatch.
 * 
 * @author Craig Walls
 */
public class JsonPatchMethodArgumentResolver extends AbstractMessageConverterMethodProcessor {

	public JsonPatchMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters) {
		super(messageConverters);
	}

	public JsonPatchMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters,
			ContentNegotiationManager contentNegotiationManager) {

		super(messageConverters, contentNegotiationManager);
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		try {
			Class.forName("org.springframework.web.patch.jsonpatch.JsonPatch");
			Class<?> paramType = parameter.getParameterType();
			return JsonPatch.class.isAssignableFrom(paramType);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		
		String name = Conventions.getVariableNameForParameter(parameter);
		Object argument = readWithMessageConverters(webRequest, parameter, JsonNode.class);

		if (binderFactory != null) {
			WebDataBinder binder = binderFactory.createBinder(webRequest, argument, name);
			argument = binder.convertIfNecessary(argument, JsonNode.class, parameter);
		}
		
		JsonNode jsonNode = (JsonNode) argument;
		return JsonPatch.fromJsonNode(jsonNode);
	}

	
	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return false;
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
	}
}
