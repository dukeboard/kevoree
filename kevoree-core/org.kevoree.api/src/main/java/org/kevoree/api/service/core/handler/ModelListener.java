/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.api.service.core.handler;


import org.kevoree.ContainerRoot;

public interface ModelListener {

    /**
     *  Method called before Kevoree Core accept an input model. Synchronized this methods is not suppose to block
     * @param currentModel
     * @param proposedModel
     * @return
     */
    public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel);

    /**
     * Method called to prepare the core to be update. Synchronized this methods can bloc Kevoree core
     * @param currentModel
     * @param proposedModel
     * @return
     */
    public boolean initUpdate(ContainerRoot currentModel, ContainerRoot proposedModel);

    /**
     *  Method called asynchronisly after a model update
     */
    public void modelUpdated();

}
