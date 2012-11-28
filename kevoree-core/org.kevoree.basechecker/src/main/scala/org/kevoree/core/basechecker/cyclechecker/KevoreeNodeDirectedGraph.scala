/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.core.basechecker.cyclechecker

import org.kevoree.framework.aspects.KevoreeAspects._

import org.jgrapht.graph.DefaultDirectedGraph
import org.kevoree._

case class KevoreeNodeDirectedGraph(model: ContainerRoot)
  extends DefaultDirectedGraph[Object, BindingFragment](new KevoreeFragmentBindingEdgeFactory) {

  model.getNodes.foreach {
    node =>
      node.getChannelFragment.foreach {
        channel =>
          val connectedNodes: List[ContainerNode] = channel.getConnectedNode(node.getName)
          if (connectedNodes.size > 0) {
            node.getComponents.foreach {
              component =>
                component.getRelatedBindings.foreach {
                  binding =>
                    if (binding.getPort.isRequiredPort
                      && binding.getPort.getPortTypeRef.getNoDependency != null &&
                      binding.getPort.getPortTypeRef.getNoDependency == false) {
                      if (binding.getHub == channel) {
                        connectedNodes.foreach {
                          node1 =>
                            node1.getComponents.foreach {
                              component1 =>
                                component1.getRelatedBindings.foreach {
                                  binding1 =>
                                    if (binding1.getHub == channel) {
                                      if (component.getProvided.contains(binding.getPort) &&
                                        component1.getRequired.contains(binding1.getPort)) {
                                        val fragment = new ChannelFragment(binding.getHub, binding)
                                        val fragment1 = new ChannelFragment(binding1.getHub, binding1)
                                        addVertex(node)
                                        addVertex(fragment)
                                        addVertex(fragment1)
                                        addVertex(node1)
                                        addEdge(node, fragment, new BindingFragment(binding, null))
                                        addEdge(fragment, fragment1, new BindingFragment(binding, binding1))
                                        addEdge(fragment1, node1, new BindingFragment(binding1, null))
                                      } else if (component1.getProvided.contains(binding1.getPort) &&
                                        component.getRequired.contains(binding.getPort)) {
                                        val fragment = new ChannelFragment(binding.getHub, binding)
                                        val fragment1 = new ChannelFragment(binding1.getHub, binding1)
                                        addVertex(node)
                                        addVertex(fragment)
                                        addVertex(fragment1)
                                        addVertex(node1)
                                        addEdge(node1, fragment1, new BindingFragment(binding1, null))
                                        addEdge(fragment1, fragment, new BindingFragment(binding1, binding))
                                        addEdge(fragment, node, new BindingFragment(binding, null))
                                      }
                                    }
                                }
                            }
                        }
                      }
                    }
                }
            }
          }
      }
  }
}
