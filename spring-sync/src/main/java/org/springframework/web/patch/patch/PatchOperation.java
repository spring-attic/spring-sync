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
package org.springframework.web.patch.patch;

import static org.springframework.web.patch.patch.PathToSpEL.*;

import java.util.List;

import org.springframework.expression.Expression;

public abstract class PatchOperation {

	protected final String op;
	
	protected final String path;
	
	protected final String spel;
	
	protected final Object value;
	
	protected final Expression spelExpression;
	
	public PatchOperation(String op, String path) {
		this(op, path, null);
	}
	
	public PatchOperation(String op, String path, Object value) {
		this.op = op;
		this.path = path;
		this.spel = pathToSpEL(path);
		this.value = value;
		this.spelExpression = spelToExpression(spel);
	}
	
	public String getOp() {
		return op;
	}
	
	public String getPath() {
		return path;
	}
	
	public Object getValue() {
		return value;
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
				throw new PatchException("Path '" + removePath + "' is not nullable.");
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
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) parentExpression.getValue(target);
			list.add(listIndex, value);
		}
	}

	public void setValueOnTarget(Object target, Object value) {
		spelExpression.setValue(target, value);
	}

	public Object getValueFromTarget(Object target) {
		return spelExpression.getValue(target);
	}

	protected Object evaluateValueFromTarget(Object targetObject) {
		return value instanceof LateObjectEvaluator ? ((LateObjectEvaluator) value).evaluate(targetObject) : value;		
	}

	// private helpers
	
	
	private Integer targetListIndex(String path) {
		String[] pathNodes = path.split("\\/");
		try {
			return Integer.parseInt(pathNodes[pathNodes.length - 1]);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	

}
