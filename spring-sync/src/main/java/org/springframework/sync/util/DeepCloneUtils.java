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
package org.springframework.sync.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;

/**
 * Utility methods for deep cloning an object graph.
 * @author Craig Walls
 */
public class DeepCloneUtils {

	/**
	 * Deep clones an object.
	 * @param original a single, non-list object to be cloned
	 * @param <T> the object's type
	 * @return the cloned object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deepClone(T original) {
		return (T) SerializationUtils.clone((Serializable) original);
	}
	
	/**
	 * Deep clones a list.
	 * @param original a list to be cloned
	 * @param <T> the list's generic type
	 * @return the cloned list
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> deepClone(List<T> original) {
		List<T> copy = new ArrayList<T>(original.size());
		for(T t : original) {
			copy.add((T) SerializationUtils.clone((Serializable) t)); 
		}
		return copy;
	}
	
}
