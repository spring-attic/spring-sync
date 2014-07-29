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

import java.util.Arrays;
import java.util.List;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JsonPatchOperation {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

	protected final String op;
	
	protected final String path;
	
	protected final String spel;
	
	protected final Expression spelExpression;
	
	public JsonPatchOperation(String op, String path) {
		this.op = op;
		this.path = path;
		this.spel = pathToSpEL(path);
		this.spelExpression = spelToExpression(spel);
	}
	
	public String getOp() {
		return op;
	}
	
	public String getPath() {
		return path;
	}
	
	abstract void perform(Object o);
	
	public Object popValueAtPath(Object target, String removePath) {
		Integer listIndex = targetListIndex(removePath);
		Expression expression = pathToExpression(removePath);
		Object value = expression.getValue(target);
		if (listIndex == null) {
			try {
				expression.setValue(target, null);
				return value;
			} catch (NullPointerException e) {
				throw new JsonPatchException("JSON path '" + removePath + "' is not nullable.");
			}
		} else {
			Expression parentExpression = spelToExpression(pathToParentSpEL(removePath));
			List<?> list = (List<?>) parentExpression.getValue(target);
			list.remove(listIndex.intValue());
			return value;
		}
	}
	
	public void addValue(Object target, Object value) {
		Expression parentExpression = spelToExpression(pathToParentSpEL(path));
		Object parent = parentExpression != null ? parentExpression.getValue(target) : null;
		Integer listIndex = targetListIndex(path);
		if (parent == null || !(parent instanceof List) || listIndex == null) {
			spelExpression.setValue(target, value);
		} else {
			try {
				List<Object> list = (List<Object>) parentExpression.getValue(target);
				
				if (value instanceof String) {
					Class<?> guessedType = guessListType(list);
					Object newItem = MAPPER.readValue((String) value, guessedType);
					list.add(listIndex, newItem);
				} else {
					list.add(listIndex, value);
				}
			} catch (Exception e) {
				// TODO: HANDLE THIS BETTER!!!
			}
		}
	}

	public void setValue(Object target, String valueJson) {
		try {
			Object currentValue = spelExpression.getValue(target);
			Class<?> currentType = currentValue != null ? currentValue.getClass() : Object.class;
			Object value = MAPPER.readValue(valueJson, currentType);
			
			spelExpression.setValue(target, value);
		} catch (SpelEvaluationException e) {
			throw new JsonPatchException("Unable to set path '" + path + "' to value " + valueJson);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JsonPatchException("Unable to set path '" + path + "' to value " + valueJson);
		}
	}

	public Object getValue(Object target) {
		return spelExpression.getValue(target);
	}

	public Expression pathToExpression(String path) {
		return SPEL_EXPRESSION_PARSER.parseExpression(pathToSpEL(path));
	}
	
	// private helpers
	
	private Class<?> guessListType(List<?> list) {
		// TODO: Guess assumes a non-empty list.
		//       If the list is empty, then this won't work.
		//       It also assumes a homogeneous list. If there are mixed types in the list, it won't work.
		//
		//       Other ways to guess:
		//       - If the list is a declared property, introspection might work
		//       - JsonPatchOperation could be explicitly given the type

		return !list.isEmpty() ? list.get(0).getClass() : null;
	}
	
	private Integer targetListIndex(String path) {
		String[] pathNodes = path.split("\\/");
		try {
			return Integer.parseInt(pathNodes[pathNodes.length - 1]);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private Expression spelToExpression(String spel) {
		return SPEL_EXPRESSION_PARSER.parseExpression(spel);
	}
	
	private String pathToSpEL(String path) {
		return pathNodesToSpEL(path.split("\\/"));
	}
	
	private String pathToParentSpEL(String path) {
		return pathNodesToSpEL(Arrays.copyOf(path.split("\\/"), path.split("\\/").length - 1));
	}
	
	private String pathNodesToSpEL(String[] pathNodes) {
		StringBuilder spelBuilder = new StringBuilder();
		
		for(int i=0; i < pathNodes.length; i++) {
			String pathNode = pathNodes[i];
			
			if (pathNode.length() == 0) {
				continue;
			}
			try {
				int index = Integer.parseInt(pathNode);
				spelBuilder.append('[').append(index).append(']');
			} catch (NumberFormatException e) {
				if (spelBuilder.length() > 0) {
					spelBuilder.append('.');	
				}
				spelBuilder.append(pathNode);
			}
		}
		
		String spel = spelBuilder.toString();
		if (spel.length() == 0) {
			spel = "#this";
		}
		return spel;		
	}
}
