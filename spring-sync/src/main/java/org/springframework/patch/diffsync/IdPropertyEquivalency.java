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
package org.springframework.patch.diffsync;

import java.lang.reflect.Field;

import org.springframework.util.ObjectUtils;

/**
 * Determines if two objects are equivalent by comparing their "id" properties.
 * 
 * @author Craig Walls
 */
public class IdPropertyEquivalency implements Equivalency {

	@Override
	public boolean isEquivalent(Object o1, Object o2) {
		try {
			Field idField1 = o1.getClass().getDeclaredField("id");
			idField1.setAccessible(true);
			Object id1 = idField1.get(o1);
			Field idField2 = o2.getClass().getDeclaredField("id");
			idField2.setAccessible(true);
			Object id2 = idField2.get(o2);
			return ObjectUtils.nullSafeEquals(id1, id2);
		} catch (NoSuchFieldException e) {
			return false;
		} catch (IllegalAccessException e) {
			return false;
		}
	}
	
}
