/*
 * Copyright 2014 the original author or authors.
 *
import org.springframework.web.patch.diffsync.PersistenceCallbackRegistry;
import org.springframework.web.patch.diffsync.ShadowStore;

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

import org.springframework.sync.diffsync.PersistenceCallback;
import org.springframework.sync.diffsync.PersistenceCallbackRegistry;
import org.springframework.sync.diffsync.ShadowStore;
import org.springframework.sync.diffsync.shadowstore.MapBasedShadowStore;

/**
 * Configurer for DiffSync.
 * @author Craig Walls
 */
public interface DiffSyncConfigurer {

	/**
	 * Override to add {@link PersistenceCallback}s.
	 * @param registry a {@link PersistenceCallbackRegistry} to which {@link PersistenceCallback}s may be registered
	 */
	void addPersistenceCallbacks(PersistenceCallbackRegistry registry);
	
	/**
	 * Override to set the {@link ShadowStore}.
	 * For convenience, a {@link MapBasedShadowStore} will be the default, but should not be used in production applications.
	 * @param remoteNodeId a unique identifier of the remote node that this shadow store is being created for. 
	 * @return a {@link ShadowStore}
	 */
	ShadowStore getShadowStore(String remoteNodeId);
	
}
