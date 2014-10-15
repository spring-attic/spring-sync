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

import java.util.List;

import org.springframework.sync.Diff;
import org.springframework.sync.Patch;
import org.springframework.sync.util.DeepCloneUtils;

/**
 * <p>
 * Implements essential steps of the Differential Synchronization routine as described in Neil Fraser's paper at https://neil.fraser.name/writing/sync/eng047-fraser.pdf.
 * </p>
 * 
 * <p>
 * The Differential Synchronization routine can be summarized as follows (with two nodes, A and B):
 * </p>
 * 
 * <ol>
 *   <li>Node A compares a resource with its local shadow of that resource to produce a patch describing the differences</li>
 *   <li>Node A replaces the shadow with the resource.</li>
 *   <li>Node A sends the difference patch to Node B.</li>
 *   <li>Node B applies the patch to its copy of the resource as well as its local shadow of the resource.</li>
 * </ol>
 * 
 * <p>
 * The routine then repeats with Node A and B swapping roles, forming a continuous loop.
 * </p>
 * 
 * <p>
 * To fully understand the Differential Synchronization routine, it's helpful to recognize that a shadow can only be changed by applying a patch or by producing a 
 * difference patch; a resource may be changed by applying a patch or by operations performed outside of the loop.
 * </p>
 * 
 * <p>
 * This class implements the handling of an incoming patch separately from the producing of the outgoing difference patch.
 * It performs no persistence of the patched resources, which is the responsibility of the caller.
 * </p>
 * 
 * @author Craig Walls
 *
 * @param <T> The entity type to perform differential synchronization against.
 */
public class DiffSync<T> {
	
	private ShadowStore shadowStore;

	private Class<T> entityType;
	
	/**
	 * Constructs the Differential Synchronization routine instance.
	 * @param shadowStore the shadow store
	 * @param entityType the type of entity this DiffSync works with
	 */
	public DiffSync(ShadowStore shadowStore, Class<T> entityType) {
		this.shadowStore = shadowStore;
		this.entityType = entityType;
	}
	
	
	/**
	 * Applies a patch to a target object and the target object's shadow, per the Differential Synchronization algorithm.
	 * The target object will remain unchanged and a patched copy will be returned.
	 * 
	 * @param patch The patch to be applied.
	 * @param target An object to apply a patch to. Will remain unchanged.
	 * @return a patched copy of the target.
	 */
	public T apply(Patch patch, T target) {
		if (patch.size() == 0) {
			return target;
		}
		
		T shadow = getShadow(target);
		shadow = patch.apply(shadow);
		putShadow(shadow);

		return patch.apply(DeepCloneUtils.deepClone(target));
	}
	
	/**
	 * Applies a patch to a target list and the target list's shadow, per the Differential Synchronization algorithm.
	 * The target object will remain unchanged and a patched copy will be returned.
	 * 
	 * @param patch The patch to be applied.
	 * @param target A list to apply a patch to. Will remain unchanged.
	 * @return a patched copy of the target.
	 */
	public List<T> apply(Patch patch, List<T> target) {
		if (patch.size() == 0) {
			return target;
		}
		
		List<T> shadow = getShadow(target);
		shadow = patch.apply(shadow);
		putShadow(shadow);

		return patch.apply(DeepCloneUtils.deepClone(target));
	}
	
	/**
	 * Compares a target object with its shadow, producing a patch describing the difference.
	 * Upon completion, the shadow will be replaced with the target, per the Differential Synchronization algorithm.
	 * @param target The target object to produce a difference patch for.
	 * @return a {@link Patch} describing the differences between the target and its shadow.
	 */
	public Patch diff(T target) {
		T shadow = getShadow(target);
		Patch diff = new Diff().diff(shadow, target);
		putShadow(diff.apply(shadow));
		return diff;
	}
	
	/**
	 * Compares a target list with its shadow, producing a patch describing the difference.
	 * Upon completion, the shadow will be replaced with the target, per the Differential Synchronization algorithm.
	 * @param target The target list to produce a difference patch for.
	 * @return a {@link Patch} describing the differences between the target and its shadow.
	 */
	public Patch diff(List<T> target) {
		List<T> shadow = getShadow(target);
		Patch diff = new Diff().diff(shadow, target);
		putShadow(diff.apply(shadow));
		return diff;
	}
	
	// private helper methods
	
	@SuppressWarnings("unchecked")
	private T getShadow(T target) {
		String shadowStoreKey = getShadowStoreKey(target);
		T shadow = (T) shadowStore.getShadow(shadowStoreKey);
		if (shadow == null) {
			shadow = DeepCloneUtils.deepClone(target);
		}
		return shadow;
	}
	
	private void putShadow(T shadow) {
		String shadowStoreKey = getShadowStoreKey(shadow);
		shadowStore.putShadow(shadowStoreKey, shadow);
	}

	@SuppressWarnings("unchecked")
	private List<T> getShadow(List<T> target) {
		String shadowStoreKey = getShadowStoreKey(target);
		List<T> shadow = (List<T>) shadowStore.getShadow(shadowStoreKey);
		if (shadow == null) {
			shadow = DeepCloneUtils.deepClone(target);
		}
		return shadow;
	}

	private void putShadow(List<T> shadow) {
		String shadowStoreKey = getShadowStoreKey(shadow);
		shadowStore.putShadow(shadowStoreKey, shadow);
	}

	
	private String getShadowStoreKey(Object o) {
		String resourceName = entityType.getSimpleName();
		if (o instanceof List) {
			return "shadow/list/" + resourceName;
		} else {
			return "shadow/" + resourceName;
		}
	}

}
