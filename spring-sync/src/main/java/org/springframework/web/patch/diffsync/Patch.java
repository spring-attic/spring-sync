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
 * Generically represents a Patch.
 * 
 * @author Craig Walls
 */
public interface Patch {

	/**
	 * @return true if the patch has no instructions and/or is effectively a no-op.
	 */
	boolean isEmpty();
	
	/**
	 * Applies the patch
	 * @param in the object to apply the patch to.
	 * @return the patched object
	 */
	Object apply(Object in);
	
}
