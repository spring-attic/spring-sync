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
package org.springframework.web.patch.diffsync;

/**
 * Strategy interface for determining if two objects are equivalent.
 * 
 * <p>During the course of a patch, a resource entity may have its properties change, thus rendering it
 * unequal to another unchanged instance of the same resource entity. Yet, for the purposes of the
 * differential synchronization algorithm, it's important to know if two entity objects are equivalent.
 * That is, it's important to know if two objects are considered to represent the same resource, even
 * if their properties are different.</p>
 * 
 * @author Craig Walls
 */
public interface Equivalency {

	/**
	 * Determines if two objects are considered equivalent (not necessarily equal).
	 * @param o1 object to compare
	 * @param o2 object to compare
	 * @return true if the two objects are considered equivalent, false otherwise.
	 */
	boolean isEquivalent(Object o1, Object o2);
	
}
